package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.ASMVariables;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.ContainerProxy;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class ContainerTransformer implements ASMClassTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		FieldNode field = new FieldNode(ACC_PRIVATE, "_sc$inventories", getDescriptor(List.class), null, null);
		clazz.fields.add(field);
		ASMVariable inventories = ASMVariables.of(clazz, field, CodePieces.getThis());

		makeGetter(clazz, inventories);

		clazz.interfaces.add(ContainerProxy.CLASS_NAME);

		return true;
	}

	private void makeGetter(ClassNode clazz, ASMVariable inventories) {
		String name = ContainerProxy.GET_INVENTORIES;
		String desc = Type.getMethodDescriptor(getType(List.class));
		MethodNode method = new MethodNode(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		clazz.methods.add(method);
		InsnList insns = method.instructions;

		inventories.get().appendTo(insns);
		insns.add(new InsnNode(DUP));

		LabelNode nonNull = new LabelNode();
		insns.add(new JumpInsnNode(IFNONNULL, nonNull));

		insns.add(new InsnNode(POP));
		String owner = ASMHooks.CLASS_NAME;
		name = ASMHooks.FIND_CONTAINER_INVS;
		desc = Type.getMethodDescriptor(getType(List.class), getObjectType(clazz.name));

		inventories.setAndGet(CodePieces.invokeStatic(owner, name, desc, CodePieces.getThis())).appendTo(insns);

		insns.add(nonNull);
		insns.add(new InsnNode(ARETURN));
	}

	@Override
	public boolean transforms(String internalName) {
		return "net/minecraft/inventory/Container".equals(internalName);
	}
}
