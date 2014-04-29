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

		generateEventCall(clazz).insertBefore(findInsertionHook(method));

		return true;
	}

	private CodePiece generateEventCall(ClassNode clazz) {
		InsnList insns = new InsnList();
		Type entityPlayer = getObjectType("net/minecraft/entity/player/EntityPlayer");
		Type entity = getObjectType("net/minecraft/entity/Entity");
		
		
		insns.add(new VarInsnNode(ALOAD, 1)); // the player
		insns.add(new VarInsnNode(ALOAD, 0));
		String name = ASMNames.field(ASMNames.F_MY_ENTITY_SRG);
		String desc = entity.getDescriptor();
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, name, desc));
		
		desc = getMethodDescriptor(VOID_TYPE, entityPlayer, entity);
		insns.add(new MethodInsnNode(INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", "onStartTracking", desc));

		return ASMUtils.asCodePiece(insns);
	}

	private CodeLocation findInsertionHook(MethodNode method) {
		String sendPacketToPlayer = ASMNames.method("func_72567_b");

		return ASMUtils.searchIn(method.instructions)
				.backwards()
				.jumpToEnd()
				.find(MethodInsnNode.class, ASMUtils.methodInsnName(), Predicates.equalTo(sendPacketToPlayer))
				.forwards()
				.find(GOTO)
				.find(GOTO)
				.startHere()
				.endHere();
	}

	@Override
	public boolean transforms(String className) {
		return "net/minecraft/entity/EntityTrackerEntry".equals(className);
	}

}
