package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.base.Predicates;
import de.take_weiland.mods.commons.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.ASMNames.M_TRY_START_WATCHING_THIS_MCP;
import static de.take_weiland.mods.commons.asm.ASMNames.M_TRY_START_WATCHING_THIS_SRG;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public class EntityTrackerEntryTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_TRY_START_WATCHING_THIS_MCP, M_TRY_START_WATCHING_THIS_SRG);

		generateEventCall(clazz)
				.insertAfter(findInsertionHook(method));

		return true;
	}

	private CodePiece generateEventCall(ClassNode clazz) {
		InsnList insns = new InsnList();
		Type entityPlayer = getObjectType("net/minecraft/entity/player/EntityPlayer");
		Type entity = getObjectType("net/minecraft/entity/Entity");
		
		
		insns.add(new VarInsnNode(ALOAD, 1)); // the player
		insns.add(new VarInsnNode(ALOAD, 0));
		String name = ASMUtils.useMcpNames() ? ASMNames.F_MY_ENTITY_MCP : ASMNames.F_MY_ENTITY_SRG;
		String desc = entity.getDescriptor();
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, name, desc));
		
		desc = getMethodDescriptor(VOID_TYPE, entityPlayer, entity);
		insns.add(new MethodInsnNode(INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", "onStartTracking", desc));
		
		return ASMUtils.asCodePiece(insns);
	}

	private CodeLocation findInsertionHook(MethodNode method) {
		String entityLivingBase = "net/minecraft/entity/EntityLivingBase";
		String netServerHandler = "net/minecraft/network/NetServerHandler";
		String sendPacketToPlayer = ASMNames.method("func_72567_b");

		CodeMatcher invokeSendPacketToPlayer = ASMUtils.asMatcher(
				Predicates.compose(
						Predicates.equalTo(sendPacketToPlayer),
						ASMUtils.methodInsnName()
				), MethodInsnNode.class).onFailure(CodeMatcher.FailureAction.RETURN_NULL);

//		System.out.println(invokeSendPacketToPlayer.findLast(method.instructions));

		CodeMatcher matcher = ASMUtils.matcher(new TypeInsnNode(INSTANCEOF, entityLivingBase))
				.andThen(invokeSendPacketToPlayer)
				.andThen(ASMUtils.matchOpcode(GOTO))
				.allowSkipping();

		return matcher.findOnly(method.instructions);
	}

	private AbstractInsnNode findInvokePacketToPlayer(AbstractInsnNode theInstanceof) {
		String sendPacketToPlayer = ASMUtils.useMcpNames() ? ASMNames.M_SEND_PACKET_TO_PLAYER_MCP : ASMNames.M_SEND_PACKET_TO_PLAYER_SRG;
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
