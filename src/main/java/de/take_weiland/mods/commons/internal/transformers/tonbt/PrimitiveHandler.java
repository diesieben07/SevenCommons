package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import static de.take_weiland.mods.commons.asm.MCPNames.*;

/**
 * @author diesieben07
 */
final class PrimitiveHandler extends ToNBTHandler {

	PrimitiveHandler(ASMVariable var) {
		super(var);
	}

	@Override
	CodePiece makeNBT() {
		Type repr = getRepresentation(var.getType());
		String className = getTagClassName(repr);
		CodePiece wrap = wrap(var.getType(), var.get());
		return CodePieces.instantiate(className,
				String.class, CodePieces.constant(""),
				repr, wrap);
	}

	@Override
	CodePiece consumeNBT(CodePiece nbt) {
		Type repr = getRepresentation(var.getType());
		String dataName;
		switch (repr.getSort()) {
			case Type.BYTE:
				dataName = MCPNames.field(F_NBT_BYTE_DATA);
				break;
			case Type.SHORT:
				dataName = MCPNames.field(F_NBT_SHORT_DATA);
				break;
			case Type.INT:
				dataName = MCPNames.field(F_NBT_INT_DATA);
				break;
			case Type.LONG:
				dataName = MCPNames.field(F_NBT_LONG_DATA);
				break;
			case Type.FLOAT:
				dataName = MCPNames.field(F_NBT_FLOAT_DATA);
				break;
			case Type.DOUBLE:
				dataName = MCPNames.field(F_NBT_DOUBLE_DATA);
				break;
			default:
				throw new AssertionError();
		}
		String className = getTagClassName(repr);

		CodePiece castedNBT = CodePieces.castTo(className, nbt);
		CodePiece data = CodePieces.getField(className, dataName, repr, castedNBT);
		return var.set(unwrap(var.getType(), data));
	}

	private static CodePiece unwrap(Type type, CodePiece raw) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return ASMCondition.isTrue(raw).doIfElse(CodePieces.constant(true), CodePieces.constant(false));
			case Type.CHAR:
				return CodePieces.castPrimitive(raw, Type.SHORT_TYPE, Type.CHAR_TYPE);
			default:
				return raw;
		}
	}

	private static CodePiece wrap(Type type, CodePiece val) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return ASMCondition.isTrue(val).doIfElse(CodePieces.constant(1), CodePieces.constant(0));
			case Type.CHAR:
				return CodePieces.castPrimitive(val, Type.CHAR_TYPE, Type.SHORT_TYPE);
			default:
				return val;
		}
	}

	private static String getTagClassName(Type type) {
		return "net/minecraft/nbt/NBTTag" + StringUtils.capitalize(type.getClassName());
	}

	private static Type getRepresentation(Type type) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return Type.BYTE_TYPE;
			case Type.CHAR:
				return Type.SHORT_TYPE;
			default:
				return type;
		}
	}
}
