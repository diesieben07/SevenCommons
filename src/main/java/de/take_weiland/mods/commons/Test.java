package de.take_weiland.mods.commons;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class Test {

	public static void transform(ClassNode clazz) {
		MethodNode clInit = findOrCreateClInit(clazz);
		InsnList hook = new InsnList();
		hook.add(new LdcInsnNode(Type.getObjectType(clazz.name)));


		hook.add(new MethodInsnNode(INVOKESTATIC, "com/example/mymod/ASMHooks", "onClassLoad", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Class.class))));
		clInit.instructions.insert(hook);

	}

	private static MethodNode findOrCreateClInit(ClassNode clazz) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<clinit>")) {
				return method;
			}
		}
		MethodNode clInit = new MethodNode(ACC_PUBLIC | ACC_STATIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
		clInit.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(clInit);
		return clInit;
	}



}
