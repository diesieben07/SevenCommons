package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.MCPNames.M_CLONE_PLAYER;

public final class EntityPlayerTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_CLONE_PLAYER);
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load this = the new player
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load the first parameter = the old player

		Type entityPlayer = Type.getObjectType(clazz.name);

		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, entityPlayer, entityPlayer);
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASMHooks.CLASS_NAME, ASMHooks.ON_PLAYER_CLONE, desc));

		AbstractInsnNode lastReturn = ASMUtils.findLastReturn(method);
		method.instructions.insertBefore(lastReturn, insns);
		return true;
	}

	@Override
	public boolean transforms(String className) {
		return className.equals("net/minecraft/entity/player/EntityPlayer");
	}
}
