package de.take_weiland.mods.commons.internal.transformers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.take_weiland.mods.commons.asm.PrependingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.ASMUtils;

public class EntityZombieTransformer extends PrependingTransformer {

	@Override
	protected InsnList getPrepends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		// load this
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		
		// call our hook. returns true if canceled
		insns.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "onZombieConvert", Type.BOOLEAN_TYPE, Type.getObjectType(clazz.name)));

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
		return "convertToVillager";
	}

	@Override
	protected String getSrgMethod() {
		return "func_82232_p";
	}

	@Override
	protected String getClassName() {
		return "net.minecraft.entity.monster.EntityZombie";
	}

}
