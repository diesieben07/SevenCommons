package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.MCPNames.M_CONVERT_TO_VILLAGER_MCP;
import static de.take_weiland.mods.commons.asm.MCPNames.M_CONVERT_TO_VILLAGER_SRG;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.SIPUSH;

public final class EntityZombieTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_CONVERT_TO_VILLAGER_MCP, M_CONVERT_TO_VILLAGER_SRG);

		makeHook(clazz).insertAfter(findTarget(method));

		return true;
	}

	private CodeLocation findTarget(MethodNode method) {
		return ASMUtils.searchIn(method.instructions)
				.find(new IntInsnNode(SIPUSH, -24000))
				.find(INVOKEVIRTUAL)
				.find(LabelNode.class)
				.startHere()
				.endHere();
	}

	private CodePiece makeHook(ClassNode clazz) {
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this, the zombie
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1)); // the villager

		// call our hook. returns null if canceled, otherwise the new villager
		Type entityVillager = Type.getObjectType("net/minecraft/entity/passive/EntityVillager");
		String name = "onZombieConvert";
		String desc = Type.getMethodDescriptor(entityVillager, Type.getObjectType(clazz.name), entityVillager);
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", name, desc));

		insns.add(new InsnNode(Opcodes.DUP)); // duplicate the villager for IFNULL check
		insns.add(new VarInsnNode(Opcodes.ASTORE, 1)); // store it in the villager variable

		LabelNode nonNull = new LabelNode();

		insns.add(new JumpInsnNode(Opcodes.IFNONNULL, nonNull)); // not null, so not canceled
		insns.add(new InsnNode(Opcodes.RETURN)); // it is null, so just return
		insns.add(nonNull);

		return ASMUtils.asCodePiece(insns);
	}

	@Override
	public boolean transforms(String className) {
		return className.equals("net/minecraft/entity/monster/EntityZombie");
	}

}
