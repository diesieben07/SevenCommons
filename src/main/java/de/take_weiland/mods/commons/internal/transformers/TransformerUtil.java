package de.take_weiland.mods.commons.internal.transformers;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.asm.info.MethodInfo;
import org.objectweb.asm.tree.*;

import static com.google.common.base.Preconditions.checkArgument;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class TransformerUtil {

	public static void addOrOverride(ClassNode clazz, ClassInfo classInfo, String name, String desc, CodePiece code, boolean superCertain) {
		checkArgument(Type.getReturnType(desc).getSort() == Type.VOID, "Only void methods supported");
		MethodNode method = ASMUtils.findMethod(clazz, name, desc);
		if (method == null) {
			method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
			clazz.methods.add(method);
			InsnList insns = method.instructions;

			if (superCertain || requiresSuperCall(classInfo, name, desc)) {
				insns.add(new VarInsnNode(ALOAD, 0));

				Type[] parameters = Type.getArgumentTypes(desc);
				int lVarIdx = 1;
				for (Type param : parameters) {
					insns.add(new VarInsnNode(param.getOpcode(ILOAD), lVarIdx));
					lVarIdx += param.getSize();
				}

				insns.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
			}

			insns.add(new InsnNode(RETURN));
		}
		code.prependTo(method.instructions);
	}

	private static boolean requiresSuperCall(ClassInfo clazz, String name, String desc) {
		clazz = clazz.superclass();
		while (clazz != null) {
			MethodInfo method = clazz.getMethod(name, desc);
			if (method != null) {
				if (method.isAbstract()) {
					return false;
				}
				if (!method.isPrivate()) {
					return true;
				}
			}
			clazz = clazz.superclass();
		}
		return false;
	}

	static AbstractInsnNode requireNext(AbstractInsnNode node) {
		if ((node = node.getNext()) == null) {
			throw new IllegalStateException("Missing next node");
		}
		return node;
	}

	static AbstractInsnNode requirePrev(AbstractInsnNode node) {
		if ((node = node.getPrevious()) == null) {
			throw new IllegalStateException("Missing previous node");
		}
		return node;
	}


}
