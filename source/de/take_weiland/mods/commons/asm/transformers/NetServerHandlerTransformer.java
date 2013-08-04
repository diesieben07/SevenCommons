package de.take_weiland.mods.commons.asm.transformers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AppendingPrependingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;

public class NetServerHandlerTransformer extends AppendingPrependingTransformer {

	@Override
	protected InsnList getAppends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // load this
		String theEntityField = ASMUtils.useMcpNames() ? "playerEntity" : "c";
		
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, theEntityField, ASMUtils.getFieldDescriptor(clazz, theEntityField)));
		insns.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "setActivePlayer", Type.VOID_TYPE, SevenCommons.ENTITY_PLAYER));
		
		return insns;
	}

	@Override
	protected InsnList getPrepends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		insns.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "resetActivePlayer", Type.VOID_TYPE));
		
		return insns;
	}
	
	@Override
	protected String getMcpMethod() {
		return "networkTick";
	}

	@Override
	protected String getSrgMethod() {
		return "func_72570_d";
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.network.NetServerHandler");
	}

}
