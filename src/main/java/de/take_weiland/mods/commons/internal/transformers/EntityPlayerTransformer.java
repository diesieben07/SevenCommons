package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.internal.ASMConstants.M_CLONE_PLAYER_MCP;
import static de.take_weiland.mods.commons.internal.ASMConstants.M_CLONE_PLAYER_SRG;

public final class EntityPlayerTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_CLONE_PLAYER_MCP, M_CLONE_PLAYER_SRG);
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load this = the new player
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load the first parameter = the old player

		Type entityPlayer = Type.getObjectType(clazz.name);

		String name = "onPlayerClone";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, entityPlayer, entityPlayer);
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", name, desc));

		AbstractInsnNode lastReturn = ASMUtils.findLastReturn(method);
		method.instructions.insertBefore(lastReturn, insns);
	}

	@Override
	public boolean transforms(String className) {
		return className.equals("net/minecraft/entity/player/EntityPlayer");
	}
}
