package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class EntityPlayerTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		String mName = ASMUtils.useMcpNames() ? ASMConstants.M_CLONE_PLAYER_MCP : ASMConstants.M_CLONE_PLAYER_SRG;
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(mName)) {
				transformClonePlayer(clazz, method);
			}
		}
	}

	private void transformClonePlayer(ClassNode clazz, MethodNode method) {
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
