package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.ASMNames.M_CONVERT_TO_VILLAGER_MCP;
import static de.take_weiland.mods.commons.asm.ASMNames.M_CONVERT_TO_VILLAGER_SRG;

public final class EntityZombieTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_CONVERT_TO_VILLAGER_MCP, M_CONVERT_TO_VILLAGER_SRG);
		method.instructions.insert(findTarget(method), makeHook(clazz));
		return true;
	}

	private AbstractInsnNode findTarget(MethodNode method) {
		AbstractInsnNode insn = method.instructions.getFirst();
		do {
			AbstractInsnNode next = next(insn);
			AbstractInsnNode nextNext = next == null ? null : next(next);
			if (insn.getOpcode() == Opcodes.SIPUSH
					&& ((IntInsnNode) insn).operand == -24000 // the SIPUSH from setGrowingAge(-24000)
					&& next != null && next.getOpcode() == Opcodes.INVOKEVIRTUAL // the call to setGrowingAge
					&& nextNext instanceof LabelNode) { // the label at the end of the if
				return nextNext;
			}
			insn = next;
		} while (insn != null);
		throw new IllegalStateException("Couldn't find hook target in EntityZombie#convertToVillager!");
	}

	private AbstractInsnNode next(AbstractInsnNode node) {
		AbstractInsnNode next = node.getNext();
		while (next != null && next.getOpcode() == -1 && !(next instanceof LabelNode)) {
			next = next.getNext();
		}
		return next;
	}

	private InsnList makeHook(ClassNode clazz) {
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

		return insns;
	}

	@Override
	public boolean transforms(String className) {
		return className.equals("net/minecraft/entity/monster/EntityZombie");
	}

}
