package de.take_weiland.mods.commons.asm.transformers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AppendingPrependingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;

public class EntityPlayerMPTransformer extends AppendingPrependingTransformer {

	@Override
	protected InsnList getAppends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		insns.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "resetActivePlayer", Type.VOID_TYPE));
		
		return insns;
	}

	@Override
	protected InsnList getPrepends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load this
		insns.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "setActivePlayer", Type.VOID_TYPE, SevenCommons.ENTITY_PLAYER));
		
		return insns;
	}
	
	@Override
	protected String getMcpMethod() {
		return "onUpdate";
	}

	@Override
	protected String getSrgMethod() {
		return "func_70071_h_";
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.entity.player.EntityPlayerMP");
	}

}
