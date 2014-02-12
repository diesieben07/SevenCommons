package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SingleMethodTransformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public class EntityTrackerEntryTransformer extends SingleMethodTransformer {

	@Override
	protected boolean transform(ClassNode clazz, MethodNode method) {
		AbstractInsnNode hook = findInsertionHook(method);
		method.instructions.insertBefore(hook, generateEventCall(clazz));

		return true;
	}

	
	private InsnList generateEventCall(ClassNode clazz) {
		InsnList insns = new InsnList();
		Type entityPlayer = getObjectType("net/minecraft/entity/player/EntityPlayer");
		Type entity = getObjectType("net/minecraft/entity/Entity");
		
		
		insns.add(new VarInsnNode(ALOAD, 1)); // the player
		insns.add(new VarInsnNode(ALOAD, 0));
		String name = ASMUtils.useMcpNames() ? ASMConstants.F_MY_ENTITY_MCP : ASMConstants.F_MY_ENTITY_SRG;
		String desc = entity.getDescriptor();
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, name, desc));
		
		desc = getMethodDescriptor(VOID_TYPE, entityPlayer, entity);
		insns.add(new MethodInsnNode(INVOKESTATIC, "de/take_weiland/mods/commons/asm/ASMHooks", "onStartTracking", desc));
		
		return insns;
	}


	private AbstractInsnNode findInsertionHook(MethodNode method) {
		String entityLivingBase = "net/minecraft/entity/EntityLivingBase";
		int len = method.instructions.size();
		for (int i = 0; i < len; ++i) {
			AbstractInsnNode insn = method.instructions.get(i);
			if (insn.getOpcode() == INSTANCEOF) {
				if (((TypeInsnNode)insn).desc.equals(entityLivingBase)) {
					return insn.getPrevious().getPrevious(); // previous is GETFIELD, then ALOAD
				}
			}
		}
		throw illegalStruct();
	}

	private RuntimeException illegalStruct() {
		return new IllegalStateException("Unexpected class structure in EntityTrackerEntry.class!");
	}

	@Override
	protected String getMcpMethod() {
		return ASMConstants.M_TRY_START_WATCHING_THIS_MCP;
	}

	@Override
	protected String getSrgMethod() {
		return ASMConstants.M_TRY_START_WATCHING_THIS_SRG;
	}

	@Override
	protected boolean transforms(String className) {
		return "net.minecraft.entity.EntityTrackerEntry".equals(className);
	}

}
