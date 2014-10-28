package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.ContainerProxy;
import net.minecraft.inventory.ICrafting;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class ContainerTransformer implements ASMClassTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		FieldNode field = new FieldNode(ACC_PRIVATE, "_sc$inventories", getDescriptor(ImmutableSet.class), null, null);
		clazz.fields.add(field);
		ASMVariable inventories = ASMVariables.of(clazz, field, CodePieces.getThis());

		makeGetter(clazz, inventories);

		clazz.interfaces.add(ContainerProxy.CLASS_NAME);

		transformAddListener(clazz);

		return true;
	}

	private static void transformAddListener(ClassNode clazz) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, MCPNames.M_ADD_CRAFTING_TO_CRAFTERS);
		String owner = ASMHooks.CLASS_NAME;
		String name = ASMHooks.ON_LISTENER_ADDED;
		String desc = Type.getMethodDescriptor(VOID_TYPE, getObjectType(clazz.name), getType(ICrafting.class));
		CodePieces.invokeStatic(owner, name, desc, CodePieces.getThis(), CodePieces.getLocal(1))
				.prependTo(method.instructions);
	}

	private static void makeGetter(ClassNode clazz, ASMVariable inventories) {
		String name = ContainerProxy.GET_INVENTORIES;
		String desc = Type.getMethodDescriptor(getType(ImmutableSet.class));
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
		desc = Type.getMethodDescriptor(getType(ImmutableSet.class), getObjectType(clazz.name));

		inventories.setAndGet(CodePieces.invokeStatic(owner, name, desc, CodePieces.getThis())).appendTo(insns);

		insns.add(nonNull);
		insns.add(new InsnNode(ARETURN));
	}

	@Override
	public boolean transforms(String internalName) {
		return "net/minecraft/inventory/Container".equals(internalName);
	}
}
