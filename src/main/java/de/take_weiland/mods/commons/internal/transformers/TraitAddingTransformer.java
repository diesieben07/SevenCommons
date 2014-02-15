package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;
import de.take_weiland.mods.commons.traits.Factory;
import de.take_weiland.mods.commons.traits.HasTraits;
import de.take_weiland.mods.commons.traits.Trait;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TraitAddingTransformer extends SelectiveTransformer {

	@Override
	protected boolean transform(ClassNode clazz, String className) {
		if (!ASMUtils.hasAnnotation(clazz, HasTraits.class)) {
			return false;
		}

		// contains the actual class and all superclasses inkl. java.lang.Object
		List<ASMUtils.ClassInfo> supers = Lists.newArrayList();

		// fetch all implemented interfaces and superinterfaces
		// and map them to their ClassNode
		Map<String, InterfaceHolder> ifaces = Maps.newHashMap();
		ASMUtils.ClassInfo current = ASMUtils.getClassInfo(clazz);
		do {
			supers.add(current);
			analyze(ifaces, current.interfaces());
			if (current.superName().equals("java/lang/Object")) {
				break;
			} else {
				current = ASMUtils.getClassInfo(current.superName(), ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
			}
		} while (true);

		supers.add(ASMUtils.getClassInfo(Object.class));


		// the init method, which will create all the impl objects
		MethodNode init = new MethodNode(Opcodes.ACC_PRIVATE, "_sc_mixins_init", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
		InsnList insns = init.instructions;

		// counter for unique impl fields
		int fieldIdx = 0;

		// get all the methods to be implemented from the interfaces
		// mapped to their return type
		Map<MethodKey, MethodHolder> methodsToImplement = Maps.newHashMap();

		for (Map.Entry<String, InterfaceHolder> entry : ifaces.entrySet()) {
			for (MethodNode method : entry.getValue().clazz.methods) {
				MethodKey key = new MethodKey(method.name, Type.getArgumentTypes(method.desc));
				MethodHolder prevReturnType = methodsToImplement.get(key);
				Type currReturnType = Type.getReturnType(method.desc);
				// no method with same signature & name so far, good
				if (prevReturnType == null) {
					methodsToImplement.put(key, new MethodHolder(currReturnType, entry.getValue().clazz));
				} else { // a method with that signature & name has already been found in another interface
					// check which of the two return types is more far down in the hierarchy
					// e.g. in InterfaceA return type is Collection but in InterfaceB its List, then we want to use List
					ASMUtils.ClassInfo prevType = ASMUtils.getClassInfo(prevReturnType.returnType.getInternalName());
					ASMUtils.ClassInfo currType = ASMUtils.getClassInfo(currReturnType.getInternalName());
					if (ASMUtils.isAssignableFrom(prevType, currType)) {
						methodsToImplement.put(key, new MethodHolder(currReturnType, entry.getValue().clazz));
					} else if (ASMUtils.isAssignableFrom(currType, prevType)) {
						methodsToImplement.put(key, new MethodHolder(prevReturnType.returnType, entry.getValue().clazz));
					} else { // the two types don't match at all
						throw new IllegalArgumentException(String.format("Conflicting Method return type %s and %s for method %s to be implemented in %s.", prevReturnType.returnType.getInternalName(), currReturnType.getInternalName(), method.name, clazz.name));
					}
				}
			}

			// add holder field for implementation of this interface
			FieldNode field = new FieldNode(Opcodes.ACC_PRIVATE, "_sc_mixins_impl" + fieldIdx++, Type.getObjectType(entry.getValue().clazz.name).getDescriptor(), null, null);
			clazz.fields.add(field);
			entry.getValue().fieldName = field.name;

			// add initialization code for this interface
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insns.add(new TypeInsnNode(Opcodes.NEW, entry.getValue().implementor.getInternalName()));
			insns.add(new InsnNode(Opcodes.DUP));
			insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, entry.getValue().implementor.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE)));
			insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, field.name, field.desc));
		}

		// add the init method to the class
		insns.add(new InsnNode(Opcodes.RETURN));
		clazz.methods.add(init);

		// now remove the methods already implemented in this class or superclasses
		for (ASMUtils.ClassInfo info : supers) {
			for (ASMUtils.MethodInfo method : info.getMethods()) {
				Type[] args = method.getArguments();
				String name = method.getName();
				MethodKey key = new MethodKey(name, args);
				MethodHolder toImplement = methodsToImplement.remove(key);
				// this is a method that implements one of the interface methods
				if (toImplement != null) {
					if (!toImplement.returnType.equals(method.getReturnType())) {
						// if one of them is primitive, don't try and check superclasses
						if (ASMUtils.isPrimitive(toImplement.returnType) || ASMUtils.isPrimitive(method.getReturnType())) {
							throw returnTypeConflict(clazz, method);
						}
						ASMUtils.ClassInfo expected = ASMUtils.getClassInfo(toImplement.returnType.getInternalName());
						ASMUtils.ClassInfo actual = ASMUtils.getClassInfo(method.getReturnType().getInternalName());
						// if method is implemented already but has wrong return type, throw
						if (!ASMUtils.isAssignableFrom(expected, actual)) {
							throw returnTypeConflict(clazz, method);
						}
					}
				}
			}
		}

		// now implement the remaining methods
		for (Map.Entry<MethodKey, MethodHolder> entry : methodsToImplement.entrySet()) {
			MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, entry.getKey().name, Type.getMethodDescriptor(entry.getValue().returnType, entry.getKey().args), null, null);
			insns = method.instructions;
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, ifaces.get(entry.getValue().declaringIface.name).fieldName, Type.getObjectType(entry.getValue().declaringIface.name).getDescriptor()));
			Type[] args = entry.getKey().args;
			int len = args.length;
			for (int i = 0; i < len; ++i) {
				Type arg = args[i];
				insns.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), i + 1));
			}

			insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, entry.getValue().declaringIface.name, method.name, method.desc));
			insns.add(new InsnNode(entry.getValue().returnType.getOpcode(Opcodes.IRETURN)));
			clazz.methods.add(method);
		}

		// Process @Factory methods
		for (MethodNode method : clazz.methods) {
			if (ASMUtils.hasAnnotation(method, Factory.class)) {
				makeFactory(clazz, method);
			}
		}

		// add a call to the initialization method to all root constructors
		for (MethodNode cnstr : ASMUtils.findRootConstructors(clazz)) {
			int len = cnstr.instructions.size();
			for (int i = 0; i < len; ++i) {
				AbstractInsnNode insn = cnstr.instructions.get(i);
				if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
					insns = new InsnList();
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz.name, init.name, init.desc));

					cnstr.instructions.insert(insn, insns);
					break;
				}
			}
		}

		clazz.access &= ~Opcodes.ACC_ABSTRACT;

		return true;
	}

	private IllegalArgumentException returnTypeConflict(ClassNode clazz, ASMUtils.MethodInfo method) {
		return new IllegalArgumentException(String.format("Confliciting Return type for already implemented method %s in class %s.", method.getName(), clazz.name));
	}

	private static class InterfaceHolder {

		final ClassNode clazz;
		final Type implementor;
		String fieldName;

		InterfaceHolder(ClassNode clazz, Type implementor) {
			this.clazz = clazz;
			this.implementor = implementor;
		}
	}

	private static class MethodKey {

		final String name;
		final Type[] args;

		MethodKey(String name, Type[] args) {
			this.name = name;
			this.args = args;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			MethodKey methodKey = (MethodKey) o;

			if (!Arrays.equals(args, methodKey.args)) return false;
			if (!name.equals(methodKey.name)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = name.hashCode();
			result = 31 * result + Arrays.hashCode(args);
			return result;
		}
	}

	private static class MethodHolder {

		final Type returnType;
		final ClassNode declaringIface;

		MethodHolder(Type returnType, ClassNode declaringIface) {
			this.returnType = returnType;
			this.declaringIface = declaringIface;
		}
	}

	private void analyze(Map<String, InterfaceHolder> collector, List<String> ifaces) {
		for (String iface : ifaces) {
			ClassNode node = ASMUtils.getClassNode(iface, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
			AnnotationNode mixin = ASMUtils.getAnnotation(node, Trait.class);
			if (mixin != null) {
				Type implementor = (Type) mixin.values.get(1);
				collector.put(iface, new InterfaceHolder(node, implementor));
			}
			analyze(collector, node.interfaces);
		}
	}

	private void makeFactory(ClassNode clazz, MethodNode method) {
		if ((method.access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC) {
			throw new IllegalArgumentException(String.format("Factory %s in class %s must be static!", method.name, clazz.name));
		}
		if (!Type.getReturnType(method.desc).getInternalName().equals(clazz.name)) {
			throw new IllegalArgumentException(String.format("Illegal return type for factory %s in class %s!", method.name, clazz.name));
		}

		InsnList insns = method.instructions;
		insns.clear();

		insns.add(new TypeInsnNode(Opcodes.NEW, clazz.name));
		insns.add(new InsnNode(Opcodes.DUP));
		Type[] args = Type.getArgumentTypes(method.desc);
		int len = args.length;
		for (int i = 0; i < len; ++i) {
			Type arg = args[i];
			insns.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), i));
		}

		insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz.name, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, args)));
		insns.add(new InsnNode(Opcodes.ARETURN));
	}

	@Override
	protected boolean transforms(String className) {
		if (className.startsWith("de.take_weiland.mods.commons.testmod_sc")) {
			return true;
		}
		return !className.startsWith("de.take_weiland.mods.commons.")
				&& !className.startsWith("cpw.mods.fml.")
				&& !className.startsWith("net.minecraftforge.")
				&& !className.startsWith("net.minecraft.");
	}

}
