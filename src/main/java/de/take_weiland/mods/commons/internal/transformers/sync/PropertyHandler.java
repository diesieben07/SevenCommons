package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;

/**
* @author diesieben07
*/
abstract class PropertyHandler {

	final ASMVariable var;
	final int idx;
	final boolean canBeNull;

	PropertyHandler(ASMVariable var, int idx) {
		this.var = var;
		this.idx = idx;
		this.canBeNull = !ASMUtils.isPrimitive(var.getType()) && !isNotNull(var);
	}

	public static PropertyHandler create(ASMVariable var, int idx) {
		Type type = var.getType();
		if (ASMUtils.isPrimitive(type)) {
			return new PrimitiveHandler(var, idx);
		}
		String internalName = type.getInternalName();
		switch (internalName) {
			case "java/lang/String":
				return new StringHandler(var, idx);
			case "java/util/EnumSet":
				return new EnumSetHandler(var, idx);
			case "java/util/BitSet":
				return new BitSetHandler(var, idx);
			case "net/minecraft/item/ItemStack":
				return new ItemStackHandler(var, idx);
			case "net/minecraftforge/fluids/FluidStack":
				return new FluidStackHandler(var, idx);

			case "java/lang/Boolean":
				return new PrimitiveBoxHandler(var, idx, Type.BOOLEAN_TYPE);
			case "java/lang/Byte":
				return new PrimitiveBoxHandler(var, idx, Type.BYTE_TYPE);
			case "java/lang/Short":
				return new PrimitiveBoxHandler(var, idx, Type.SHORT_TYPE);
			case "java/lang/Character":
				return new PrimitiveBoxHandler(var, idx, Type.CHAR_TYPE);
			case "java/lang/Integer":
				return new PrimitiveBoxHandler(var, idx, Type.INT_TYPE);
			case "java/lang/Long":
				return new PrimitiveBoxHandler(var, idx, Type.LONG_TYPE);
			case "java/lang/Float":
				return new PrimitiveBoxHandler(var, idx, Type.FLOAT_TYPE);
			case "java/lang/Double":
				return new PrimitiveBoxHandler(var, idx, Type.DOUBLE_TYPE);
		}

		ClassInfo classInfo = ClassInfo.of(type);
		if (classInfo.isEnum()) {
			return new EnumHandler(var, idx);
		} else if (ClassInfo.of(Block.class).isAssignableFrom(classInfo)) {
			return new BlockHandler(var, idx);
		} else if (ClassInfo.of(Item.class).isAssignableFrom(classInfo)) {
			return new ItemHandler(var, idx);
		} else {
			return new HandlerWithWatcher(var, idx);
		}
	}

	private static boolean isNotNull(ASMVariable var) {
		return var.getterAnnotation(NotNull.class) != null
				|| var.getterAnnotation(de.take_weiland.mods.commons.sync.NotNull.class) != null
				|| var.getterAnnotation("javax/annotation/Nonnull") != null;
	}

	abstract void initialTransform(TransformState state);

	abstract ASMCondition hasChanged();

	abstract CodePiece writeAndUpdate(CodePiece stream);

	abstract CodePiece read(CodePiece stream);

	final ASMVariable createCompanion(TransformState state) {
		return createCompanion(state, var.getType());
	}

	final ASMVariable createCompanion(TransformState state, Type type) {
		String name = SyncTransformer.companionName(var);
		String desc = type.getDescriptor();
		FieldNode field = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		state.clazz.fields.add(field);
		return ASMVariables.of(state.clazz, field, CodePieces.getThis());
	}
}
