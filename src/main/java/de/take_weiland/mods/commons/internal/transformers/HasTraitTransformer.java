package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.trait.HasTrait;
import de.take_weiland.mods.commons.trait.Instance;
import de.take_weiland.mods.commons.trait.Trait;
import de.take_weiland.mods.commons.trait.TraitMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author diesieben07
 */
public class HasTraitTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		if (!ASMUtils.hasAnnotation(clazz, HasTrait.class)) {
			return;
		}

		Map<String, TraitInfo> allTraits = findTraits(clazz);

		for (MethodNode method : clazz.methods) {
			AnnotationNode trMethod = ASMUtils.getAnnotation(method, TraitMethod.class);
			if (trMethod == null) {
				continue;
			}
			Type traitType = trMethod.values == null || trMethod.values.size() == 0 ? null : (Type) trMethod.values.get(1);
			TraitInfo trait;
			if (traitType == null) {
				trait = findTraitDeclarator(clazz, method, allTraits.values());
			} else {
				trait = allTraits.get(traitType.getInternalName());
				if (trait == null) {
					throw new IllegalArgumentException(String.format("Illegal Trait class on @TraitMethod %s in class %s", method.name, clazz.name));
				}
			}
			implementMethod(method, trait);
		}

		ASMUtils.ClassInfo me = ASMUtils.getClassInfo(clazz);

		Collection<MethodNode> myCstrs = ASMUtils.findRootConstructors(clazz);
		int fieldIdx = 0;
		for (TraitInfo traitInfo : allTraits.values()) {
			for (FieldNode field : traitInfo.impl.fields) {
				if (ASMUtils.hasAnnotation(field, Instance.class)) {
					Type req = Type.getType(field.desc);
					if (!ASMUtils.isAssignableFrom(ASMUtils.getClassInfo(req.getInternalName()), me)) {
						throw new IllegalArgumentException(String.format("@HasTrait class %s doesn't meet requirement %s for trait %s", clazz.name, req.getInternalName(), traitInfo.clazz.name));
					}
				}
			}

			if (!traitInfo.isDirect) {
				continue;
			}

			String traitCstrDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(traitInfo.clazz.name));
			for (MethodNode myCstr : myCstrs) {
				InsnList call = new InsnList();
				call.add(new VarInsnNode(Opcodes.ALOAD, 0));
				call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, traitInfo.impl.name, TraitUtil.constructorName, traitCstrDesc));
				AbstractInsnNode insn = myCstr.instructions.getFirst();
				while (insn.getOpcode() != Opcodes.INVOKESPECIAL) {
					insn = insn.getNext();
				}
				myCstr.instructions.insert(insn, call);
			}

			for (FieldNode field : traitInfo.impl.fields) {
				if ((field.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC || ASMUtils.hasAnnotation(field, Instance.class)) {
					continue;
				}

				FieldNode myField = new FieldNode(Opcodes.ACC_PRIVATE, "_sc_trait_field_" + fieldIdx++, field.desc, null, null);
				clazz.fields.add(myField);

				Type fieldType = Type.getType(field.desc);
				String name = TraitUtil.getGetterName(traitInfo.clazz.name, field.name);
				String desc = Type.getMethodDescriptor(fieldType);
				MethodNode getter = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, null, null);
				InsnList insns = getter.instructions;

				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, myField.name, myField.desc));

				insns.add(new InsnNode(fieldType.getOpcode(Opcodes.IRETURN)));

				clazz.methods.add(getter);

				name = TraitUtil.getSetterName(traitInfo.clazz.name, field.name);
				desc = Type.getMethodDescriptor(Type.VOID_TYPE, fieldType);
				MethodNode setter = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, null, null);
				insns = setter.instructions;

				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insns.add(new VarInsnNode(fieldType.getOpcode(Opcodes.ILOAD), 1));
				insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, myField.name, myField.desc));

				insns.add(new InsnNode(Opcodes.RETURN));

				clazz.methods.add(setter);
			}
		}
	}

	private void implementMethod(MethodNode method, TraitInfo trait) {
		InsnList insns = method.instructions;
		insns.clear();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		Type[] args = Type.getArgumentTypes(method.desc);
		int len = args.length;
		for (int i = 0; i < len; ++i) {
			insns.add(new VarInsnNode(args[i].getOpcode(Opcodes.ILOAD), i + 1));
		}

		Type returnType = Type.getReturnType(method.desc);
		String desc = Type.getMethodDescriptor(returnType, ObjectArrays.concat(Type.getObjectType(trait.clazz.name), args));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, trait.impl.name, method.name, desc));
		insns.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
	}

	private TraitInfo findTraitDeclarator(ClassNode clazz, MethodNode method, Collection<TraitInfo> allInterfaces) {
		TraitInfo candidate = null;
		for (TraitInfo trait : allInterfaces) {
			for (MethodNode traitMethod : trait.clazz.methods) {
				if (traitMethod.name.equals(method.name) && traitMethod.desc.equals(method.desc)) {
					if (candidate == null) {
						candidate = trait;
					} else {
						throw new IllegalArgumentException(String.format("Multiple candidates for @TraitMethod %s in class %s. Please specify which one to use!", method.name, clazz.name));
					}
				}
			}
		}
		if (candidate == null) {
			throw new IllegalArgumentException(String.format("Method %s in class %s has @TraitMethod but is not from a Trait!", method.name, clazz.name));
		}
		return candidate;
	}

	private Map<String, TraitInfo> findTraits(ClassNode clazz) {
		Map<String, TraitInfo> traits = Maps.newHashMap();
		boolean isDirect = true;
		do {
			collectTraits(clazz.interfaces, traits, isDirect);
			if (!clazz.superName.equals("java/lang/Object")) {
				clazz = ASMUtils.getThinClassNode(clazz.superName);
				isDirect = false;
			} else {
				clazz = null;
			}
		} while (clazz != null);

		return traits;
	}

	private void collectTraits(Collection<String> ifaces, Map<String, TraitInfo> all, boolean isDirect) {
		for (String iface : ifaces) {
			TraitInfo prev;
			if ((prev = all.get(iface)) != null) {
				if (!isDirect) {
					prev.isDirect = false;
				}
				continue;
			}
			ClassNode ifaceNode = ASMUtils.getThinClassNode(iface);
			if (ASMUtils.hasAnnotation(ifaceNode, Trait.class)) {
				all.put(iface, new TraitInfo(isDirect, ifaceNode));
			}
			collectTraits(ifaceNode.interfaces, all, isDirect);
		}
	}

	private static class TraitInfo {

		boolean isDirect;
		final ClassNode clazz;
		final ClassNode impl;

		TraitInfo(boolean isDirect, ClassNode clazz) {
			this.isDirect = isDirect;
			this.clazz = clazz;
			impl = ASMUtils.getThinClassNode(((Type) ASMUtils.getAnnotation(clazz, Trait.class).values.get(1)).getInternalName());
		}
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/");
	}
}
