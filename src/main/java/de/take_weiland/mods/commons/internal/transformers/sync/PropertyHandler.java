package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.*;

/**
* @author diesieben07
*/
abstract class PropertyHandler {

	final ASMVariable var;
	final int idx;
	final TransformState state;

	PropertyHandler(ASMVariable var, int idx, TransformState state) {
		this.var = var;
		this.idx = idx;
		this.state = state;
	}

	public static PropertyHandler create(TransformState state, ASMVariable var, int idx) {
		Type type = var.getType();
		if (ASMUtils.isPrimitive(type)) {
			return new PrimitiveHandler(var, idx, state);
		}
		String internalName = type.getInternalName();
		switch (internalName) {
			case "java/lang/Boolean":
				return new PrimitiveBoxHandler(var, idx, Type.BOOLEAN_TYPE, state);
			case "java/lang/Byte":
				return new PrimitiveBoxHandler(var, idx, Type.BYTE_TYPE, state);
			case "java/lang/Short":
				return new PrimitiveBoxHandler(var, idx, Type.SHORT_TYPE, state);
			case "java/lang/Character":
				return new PrimitiveBoxHandler(var, idx, Type.CHAR_TYPE, state);
			case "java/lang/Integer":
				return new PrimitiveBoxHandler(var, idx, Type.INT_TYPE, state);
			case "java/lang/Long":
				return new PrimitiveBoxHandler(var, idx, Type.LONG_TYPE, state);
			case "java/lang/Float":
				return new PrimitiveBoxHandler(var, idx, Type.FLOAT_TYPE, state);
			case "java/lang/Double":
				return new PrimitiveBoxHandler(var, idx, Type.DOUBLE_TYPE, state);
		}

		return new HandlerSyncer(var, idx, state);
	}

	abstract void initialTransform();

	abstract ASMCondition hasChanged();

	abstract CodePiece writeAndUpdate(CodePiece stream);

	abstract CodePiece write(CodePiece stream);

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
