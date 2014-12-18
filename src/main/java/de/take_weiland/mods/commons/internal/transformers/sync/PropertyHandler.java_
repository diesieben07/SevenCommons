package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.sync.SyncContents;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.lang.annotation.Annotation;

import static org.objectweb.asm.Opcodes.*;

/**
* @author diesieben07
*/
abstract class PropertyHandler {

	final ASMVariable var;
	final int idx;

	PropertyHandler(ASMVariable var, int idx) {
		this.var = var;
		this.idx = idx;
	}

	public static PropertyHandler create(TransformState state, ASMVariable var, int idx) {
		PropertyHandler handler = create0(var, idx);
		if (var.getterAnnotation(handler.getAnnotation()) == null) {
			throw new RuntimeException("Don't know how to @Sync property \"" + var.rawName() + "\" in " + state.clazz.name);
		}
		return handler;
	}

	private static PropertyHandler create0(ASMVariable var, int idx) {
		Type type = var.getType();
		if (ASMUtils.isPrimitive(type)) {
			return new PrimitiveHandler(var, idx);
		}
		String internalName = type.getInternalName();
		switch (internalName) {
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

		if (var.getterAnnotation(SyncContents.class) != null) {
			return new HandlerContentSyncer(var, idx);
		} else {
			return new HandlerValueSyncer(var, idx);
		}
	}

	private static boolean isNotNull(ASMVariable var) {
		return var.getterAnnotation(NotNull.class) != null
				|| var.getterAnnotation("javax/annotation/Nonnull") != null;
	}

	abstract Class<? extends Annotation> getAnnotation();

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
