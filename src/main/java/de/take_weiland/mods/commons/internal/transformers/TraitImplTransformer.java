package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * @author diesieben07
 */
public class TraitImplTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		int traitImplIdx;
		if ((traitImplIdx = clazz.interfaces.indexOf("de/take_weiland/mods/commons/trait/TraitImpl")) == -1) {
			return;
		}
		if ((clazz.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE || (clazz.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
			throw new IllegalArgumentException(String.format("TraitImpl class %s may not be interface or abstract!", clazz.name));
		}
		if (!clazz.superName.equals("java/lang/Object")) {
			throw new IllegalArgumentException(String.format("TraitImpl class %s may only inherit from Object!", clazz.name));
		}
		if (clazz.interfaces.size() != 2) {
			throw new IllegalArgumentException(String.format("TraitImpl class %s may only implement one trait!", clazz.name));
		}

		ClassNode traitIface = ASMUtils.getClassNode(clazz.interfaces.get(traitImplIdx == 0 ? 1 : 0), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		String cstrDesc = Type.getMethodDescriptor(Type.VOID_TYPE);
		for (MethodNode method : clazz.methods) {
			if ((method.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
				continue;
			}

			boolean isConstructor = method.name.equals("<init>");
			if (isConstructor) {
				if (!method.desc.equals(cstrDesc)) {
					throw new IllegalArgumentException(String.format("TraitImpl class %s may only specify the default constructor!", clazz.name));
				}
				method.name = TraitUtil.constructorName;
				AbstractInsnNode insn = method.instructions.getFirst();
				while (insn.getOpcode() != Opcodes.INVOKESPECIAL) {
					AbstractInsnNode me = insn;
					insn = insn.getNext();
					method.instructions.remove(me);
				}
				method.instructions.remove(insn);
			}

			if (isConstructor || isOverriden(method, traitIface)) {
				changeAccess(method, false);
			} else {
				if ((method.access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
					throw new IllegalArgumentException(String.format("Method %s in TraitImpl class %s must be private because it doesn't implement anything!", method.name, clazz.name));
				}
				changeAccess(method, true);
			}
			method.desc = transformDescriptor(traitIface, method.desc);
			transformInstructions(clazz, traitIface, method);
		}

		String name = "<init>";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		MethodNode dfltCstr = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, null, null);
		InsnList insns = dfltCstr.instructions;
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", name, desc));
		insns.add(new InsnNode(Opcodes.RETURN));

		clazz.methods.add(dfltCstr);

		for (Iterator<FieldNode> it = clazz.fields.iterator(); it.hasNext();) {
			if ((it.next().access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC) {
				it.remove();
			}
		}

		clazz.interfaces.clear();
	}

	private void transformInstructions(ClassNode implClass, ClassNode trait, MethodNode method) {
		if (method.localVariables != null) {
			for (LocalVariableNode lv : method.localVariables) {
				if (lv.index == 0) {
					lv.name = "self";
					break;
				}
			}
		}
		InsnList insns = method.instructions;
		AbstractInsnNode insn = insns.getFirst();
		do {
			int op = insn.getOpcode();
			if (op == Opcodes.INVOKEVIRTUAL) {
				transformVirtualCall(implClass, trait, (MethodInsnNode) insn);
			} else if (op == Opcodes.INVOKESPECIAL) {
				transformSpecialCall(implClass, trait, (MethodInsnNode) insn);
			} else if (op == Opcodes.GETFIELD) {
				insn = transformGetfield(implClass, trait, insns, (FieldInsnNode) insn);
			} else if (op == Opcodes.PUTFIELD) {
				insn = transformSetfield(implClass, trait, insns, (FieldInsnNode) insn);
			}

			insn = insn.getNext();
		} while (insn != null);
	}

	private AbstractInsnNode transformGetfield(ClassNode implClass, ClassNode trait, InsnList insns, FieldInsnNode insn) {
		if (insn.owner.equals(implClass.name)) {
			String name = TraitUtil.getGetterName(trait.name, insn.name);
			String desc = Type.getMethodDescriptor(Type.getType(insn.desc));
			MethodInsnNode min = new MethodInsnNode(Opcodes.INVOKEINTERFACE, trait.name, name, desc);
			insns.set(insn, min);
			return min;
		}
		return insn;
	}

	private AbstractInsnNode transformSetfield(ClassNode implClass, ClassNode trait, InsnList insns, FieldInsnNode insn) {
		if (insn.owner.equals(implClass.name)) {
			String name = TraitUtil.getSetterName(trait.name, insn.name);
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(insn.desc));
			MethodInsnNode min = new MethodInsnNode(Opcodes.INVOKEINTERFACE, trait.name, name, desc);
			insns.set(insn, min);
			return min;
		}
		return insn;
	}

	private void transformVirtualCall(ClassNode implClass, ClassNode trait, MethodInsnNode insn) {
		if (insn.owner.equals(implClass.name)) {
			insn.setOpcode(Opcodes.INVOKEINTERFACE);
			insn.owner = trait.name;
		}
	}

	private void transformSpecialCall(ClassNode implClass, ClassNode trait, MethodInsnNode insn) {
		if (insn.owner.equals(implClass.name)) {
			insn.setOpcode(Opcodes.INVOKESTATIC);
			insn.desc = transformDescriptor(trait, insn.desc);
		}
	}

	private String transformDescriptor(ClassNode trait, String desc) {
		Type[] args = Type.getArgumentTypes(desc);
		Type returnType = Type.getReturnType(desc);
		Type me = Type.getObjectType(trait.name);
		return Type.getMethodDescriptor(returnType, ObjectArrays.concat(me, args));
	}

	private void changeAccess(MethodNode method, boolean makePrivate) {
		method.access = (method.access | Opcodes.ACC_STATIC | (makePrivate ? Opcodes.ACC_PRIVATE : Opcodes.ACC_PUBLIC)) & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
	}

	private boolean isOverriden(MethodNode method, ClassNode trait) {
		for (MethodNode tMethod : trait.methods) {
			if (method.name.equals(tMethod.name) && method.desc.equals(tMethod.desc)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/");
	}
}
