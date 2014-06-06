package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public final class Conditions {

	public static ASMCondition ifTrue(CodePiece piece) {
		return of(piece, IFNE, IFEQ);
	}

	public static ASMCondition of(final CodePiece conditionArgs, final int opcode, final int opcodeNegated) {
		return new ConditionImpl(conditionArgs, opcode, opcodeNegated);
	}

	public static ASMCondition ifNull(final CodePiece value) {
		return of(value, IFNULL, IFNONNULL);
	}

	public static ASMCondition ifEqual(CodePiece a, CodePiece b, Type type) {
		return ifEqual(a, b, type, false);
	}

	public static ASMCondition ifEqual(CodePiece a, CodePiece b, Type type, boolean useEquals) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return of(a.append(b).append(CodePieces.ofOpcode(IXOR)), IFEQ, IFNE);
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.CHAR:
				return of(a.append(b), IF_ICMPEQ, IF_ICMPNE);
			case Type.LONG:
				return of(a.append(b).append(CodePieces.ofOpcode(LCMP)), IFEQ, IFNE);
			case Type.FLOAT:
				return of(a.append(b).append(CodePieces.ofOpcode(FCMPL)), IFEQ, IFNE);
			case Type.DOUBLE:
				return of(a.append(b).append(CodePieces.ofOpcode(DCMPL)), IFEQ, IFNE);
			case Type.OBJECT:
				if (!useEquals) {
					return of(a.append(b), IF_ACMPEQ, IF_ACMPNE);
				} else {
					Type objectType = Type.getType(Object.class);
					return ifTrue(
							CodePieces.invokeStatic("com/google/common/base/Objects",
									"equal",
									getMethodDescriptor(BOOLEAN_TYPE, objectType, objectType),
									a, b));
				}
			case Type.ARRAY:
				if (!useEquals) {
					return of(a.append(b), IF_ACMPEQ, IF_ACMPNE);
				} else {
					String mName = type.getDimensions() == 1 ? "equals" : "deepEquals";
					String desc = type.getDimensions() == 1 ?
							getMethodDescriptor(BOOLEAN_TYPE, type, type) :
							getMethodDescriptor(BOOLEAN_TYPE, getType(Object[].class), getType(Object[].class));

					return ifTrue(
							CodePieces.invokeStatic("java/util/Arrays",
									mName,
									desc,
									a, b));
				}
			default:
				throw new IllegalArgumentException("Invalid Type for comparision!");
		}
	}

	private Conditions() { }
}
