package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.PrependingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class EntityZombieTransformer extends PrependingTransformer {

	@Override
	protected InsnList getPrepends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		// load this
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		
		// call our hook. returns true if canceled
		String name = "onZombieConvert";
		String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType(clazz.name));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", name, desc));

		LabelNode skipReturn = new LabelNode();
//		
		// if zero returned (= false => don't cancel) skip the canceling
		insns.add(new JumpInsnNode(Opcodes.IFEQ, skipReturn));
		
		// cancel the event (just return from the method)
		insns.add(new InsnNode(Opcodes.RETURN));
		
		// continue here if not canceled
		insns.add(skipReturn);
		
		return insns;
	}

	@Override
	protected String getMcpMethod() {
		return ASMConstants.M_CONVERT_TO_VILLAGER_MCP;
	}

	@Override
	protected String getSrgMethod() {
		return ASMConstants.M_CONVERT_TO_VILLAGER_SRG;
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.entity.monster.EntityZombie");
	}

}
