package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AppendingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class EntityPlayerTransformer extends AppendingTransformer {

	@Override
	protected InsnList getAppends(ClassNode clazz, MethodNode method) {
		
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load this = the new player
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load the first parameter = the old player
		
		Type entityPlayer = Type.getObjectType(clazz.name);

		String name = "onPlayerClone";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, entityPlayer, entityPlayer);
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", name, desc));
		
		return insns;
	}

	@Override
	protected String getMcpMethod() {
		return ASMConstants.M_CLONE_PLAYER_MCP;
	}

	@Override
	protected String getSrgMethod() {
		return ASMConstants.M_CLONE_PLAYER_SRG;
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.entity.player.EntityPlayer");
	}
}
