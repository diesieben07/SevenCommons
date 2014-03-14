package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.internal.ItemStackProxy;
import de.take_weiland.mods.commons.metadata.Metadata;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class ItemStackTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		FieldNode field = new FieldNode(ACC_PRIVATE, "_sc$itemStackMetadata", getDescriptor(Metadata.class), null, null);
		clazz.fields.add(field);

		MethodNode method = new MethodNode(ACC_PUBLIC, ItemStackProxy.GETTER, getMethodDescriptor(getType(Metadata.class)), null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
		method.instructions.add(new InsnNode(ARETURN));
		clazz.methods.add(method);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/ItemStackProxy");
		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return internalName.equals("net/minecraft/item/ItemStack");
	}
}
