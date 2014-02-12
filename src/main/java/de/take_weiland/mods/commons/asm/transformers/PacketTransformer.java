package de.take_weiland.mods.commons.asm.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;
import de.take_weiland.mods.commons.network.ModPacket;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class PacketTransformer extends SelectiveTransformer {

	@Override
	protected boolean transform(ClassNode clazz, String className) {
		if ((clazz.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
			return false;
		}
		
		try {
			Class<?> superClass = getClass().getClassLoader().loadClass(ASMUtils.undoInternalName(clazz.superName));
			if (!ModPacket.class.isAssignableFrom(superClass) || hasDefaultConstructor(clazz)) {
				return false;
			}
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		MethodNode constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
		constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		constructor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz.superName, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE)));
		constructor.instructions.add(new InsnNode(Opcodes.RETURN));
		
		clazz.methods.add(constructor);
		
		return true;
	}
	
	private static final boolean hasDefaultConstructor(ClassNode clazz) {
		String searchForDesc = Type.getMethodDescriptor(Type.VOID_TYPE);
		
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<init>") && method.desc.equals(searchForDesc)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean transforms(String className) {
		return !className.equals("de.take_weiland.mods.commons.network.ModPacket");
	}

}
