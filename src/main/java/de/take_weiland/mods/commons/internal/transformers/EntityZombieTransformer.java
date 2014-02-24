package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class EntityZombieTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		String mName = ASMUtils.useMcpNames() ? ASMConstants.M_CONVERT_TO_VILLAGER_MCP : ASMConstants.M_CONVERT_TO_VILLAGER_SRG;
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(mName)) {
				transformConvertToVillager(clazz, method);
				break;
			}
		}
	}

	private void transformConvertToVillager(ClassNode clazz, MethodNode method) {
		AbstractInsnNode insn = method.instructions.getFirst();
		do {
			AbstractInsnNode next = next(insn);
			AbstractInsnNode nextNext = next == null ? null : next(next);
			if (insn.getOpcode() == Opcodes.SIPUSH
					&& ((IntInsnNode) insn).operand == -24000 // the SIPUSH from setGrowingAge(-24000)
					&& next != null && next.getOpcode() == Opcodes.INVOKEVIRTUAL // the call to setGrowingAge
					&& nextNext instanceof LabelNode) { // the label at the end of the if
				method.instructions.insert(nextNext, makeHook(clazz));
				return;
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
