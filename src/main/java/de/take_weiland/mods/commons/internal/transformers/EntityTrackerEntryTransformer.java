package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.internal.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.*;

public class EntityTrackerEntryTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		String mName = ASMUtils.useMcpNames() ? ASMConstants.M_TRY_START_WATCHING_THIS_MCP : ASMConstants.M_TRY_START_WATCHING_THIS_SRG;
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(mName)) {
				System.out.println("hey tehre!");
				method.instructions.insertBefore(findInsertionHook(method), generateEventCall(clazz));
				return;
			}
		}
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
		insns.add(new MethodInsnNode(INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", "onStartTracking", desc));
		
		return insns;
	}


	private AbstractInsnNode findInsertionHook(MethodNode method) {
		String entityLivingBase = "net/minecraft/entity/EntityLivingBase";

		AbstractInsnNode insn = method.instructions.getLast();
		do {
			if (insn.getOpcode() == INSTANCEOF && ((TypeInsnNode) insn).desc.equals(entityLivingBase)) {
				AbstractInsnNode invokePacketToPlayer = findInvokePacketToPlayer(insn);
				return findGoto(invokePacketToPlayer);
			}
			insn = insn.getPrevious();
		} while (insn != null);
		throw illegalStruct();
	}

	private AbstractInsnNode findInvokePacketToPlayer(AbstractInsnNode theInstanceof) {
		String sendPacketToPlayer = ASMUtils.useMcpNames() ? ASMConstants.M_SEND_PACKET_TO_PLAYER_MCP : ASMConstants.M_SEND_PACKET_TO_PLAYER_SRG;
		AbstractInsnNode node = theInstanceof.getNext();
		do {
			if (node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(sendPacketToPlayer)) {
				return node;
			}
			node = node.getNext();
		} while (node != null);
		throw illegalStruct();
	}

	private AbstractInsnNode findGoto(AbstractInsnNode theInvokeVirtual) {
		AbstractInsnNode node = theInvokeVirtual.getNext();
		boolean foundOne = false;
		do {
			if (node.getOpcode() == GOTO) {
				if (foundOne) {
					return node;
				}
				foundOne = true;
			}
			node = node.getNext();
		} while (node != null);
		throw illegalStruct();
	}

	private IllegalStateException illegalStruct() {
		return new IllegalStateException("Unexpected class structure in EntityTrackerEntry.class!");
	}

	@Override
	public boolean transforms(String className) {
		return "net/minecraft/entity/EntityTrackerEntry".equals(className);
	}

}
