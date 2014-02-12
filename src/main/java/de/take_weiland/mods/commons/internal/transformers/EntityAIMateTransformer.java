package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AppendingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.ASMConstants.*;

public final class EntityAIMateTransformer extends AppendingTransformer {

	@Override
	protected InsnList getAppends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		// load this.theAnimal
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		String fieldTheAnimal = ASMUtils.useMcpNames() ? F_THE_ANIMAL_MCP : F_THE_ANIMAL_OBF;
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, fieldTheAnimal, ASMUtils.getFieldDescriptor(clazz, fieldTheAnimal)));
		
		// load this.targetMate
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		String fieldMate = ASMUtils.useMcpNames() ? F_TARGET_MATE_MCP : F_TARGET_MATE_OBF;
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, fieldMate, ASMUtils.getFieldDescriptor(clazz, fieldMate)));
		
		// load local var entityageable (the baby)
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		
		Type entityAnimal = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.passive.EntityAnimal"));
		Type entityAgeable = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.EntityAgeable"));
		
		// call our hook
		String name = "onLivingBreed";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, entityAnimal, entityAnimal, entityAgeable);
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", name, desc));

		return insns;
	}

	@Override
	protected String getMcpMethod() {
		return M_SPAWN_BABY_MCP;
	}

	@Override
	protected String getSrgMethod() {
		return M_SPAWN_BABY_SRG;
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.entity.ai.EntityAIMate");
	}

}
