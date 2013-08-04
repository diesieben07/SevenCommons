package de.take_weiland.mods.commons.asm.transformers;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AppendingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;

public class EntityAIMateTransformer extends AppendingTransformer {

	@Override
	protected InsnList getAppends(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();
		
		// load this.theAnimal
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		String fieldTheAnimal = ASMUtils.useMcpNames() ? "theAnimal" : "d";
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, fieldTheAnimal, ASMUtils.getFieldDescriptor(clazz, fieldTheAnimal)));
		
		// load this.targetMate
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		String fieldMate = ASMUtils.useMcpNames() ? "targetMate" : "e";
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, fieldMate, ASMUtils.getFieldDescriptor(clazz, fieldMate)));
		
		// load local var entityageable (the baby)
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		
		Type entityAnimal = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.passive.EntityAnimal"));
		Type entityAgeable = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.EntityAgeable"));
		
		// call our hook
		insns.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "onLivingBreed", Type.VOID_TYPE, entityAnimal, entityAnimal, entityAgeable));
		
		return insns;
	}

	@Override
	protected String getMcpMethod() {
		return "spawnBaby";
	}

	@Override
	protected String getSrgMethod() {
		return "func_75388_i";
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.entity.ai.EntityAIMate");
	}

}
