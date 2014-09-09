package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
public abstract class ASMCondition {

	public static ASMCondition ifSame(CodePiece a, CodePiece b, Type type) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
			case Type.BYTE:
			case Type.SHORT:
			case Type.CHAR:
			case Type.INT:
				return new StandardCondition(a.append(b), IF_ICMPEQ);
			case Type.LONG:
				return new StandardCondition(a.append(b).append(new InsnNode(LCMP)), IFEQ);
			case Type.FLOAT:
				return new StandardCondition(a.append(b).append(new InsnNode(FCMPG)), IFEQ);
			case Type.DOUBLE:
				return new StandardCondition(a.append(b).append(new InsnNode(DCMPG)), IFEQ);
			case Type.OBJECT:
			case Type.ARRAY:
				return new StandardCondition(a.append(b), IF_ACMPEQ);
			default:
				throw new IllegalArgumentException("Invalid Type!");
		}
	}

	public static ASMCondition ifEqual(CodePiece a, CodePiece b, Type type) {
		if (type.getSort() == Type.ARRAY) {
			String owner = "java/util/Arrays";
			String name;
			String desc;
			if (type.getDimensions() != 1) {
				name = "deepEquals";
				desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Object[].class), getType(Object[].class));
			} else {
				name = "equals";
				if (ASMUtils.isPrimitive(type.getElementType())) {
					desc = getMethodDescriptor(BOOLEAN_TYPE, type, type);
				} else {
					desc = getMethodDescriptor(BOOLEAN_TYPE, getType(Object[].class), getType(Object[].class));
				}
			}
			return ifTrue(CodePieces.invokeStatic(owner, name, desc, a, b));
		} else if (type.getSort() == Type.OBJECT) {
			CodePiece equal = CodePieces.invoke(INVOKEVIRTUAL, "java/lang/Object", "equals", getMethodDescriptor(BOOLEAN_TYPE, getType(Object.class)), a, b);
			return ifNotNull(a).and(ifTrue(equal));
		} else {
			// primitives, or invalid
			return ifSame(a, b, type);
		}
	}

	public static ASMCondition ifNull(CodePiece value) {
		return new StandardCondition(value, IFNULL);
	}

	public static ASMCondition ifNotNull(CodePiece value) {
		return new StandardCondition(value, IFNONNULL);
	}

	public static ASMCondition ifTrue(CodePiece booleanValue) {
		return new StandardCondition(booleanValue, IFNE);
	}

	public static ASMCondition ifFalse(CodePiece booleanValue) {
		return new StandardCondition(booleanValue, IFEQ);
	}

	public ASMCondition or(ASMCondition other) {
		return other.invokeOrCombination(this);
	}

	public ASMCondition and(ASMCondition other) {
		return other.invokeAndCombination(this);
	}

	public abstract ASMCondition negate();

	public CodePiece doIfTrue(CodePiece code) {
		CodeBuilder cb = new CodeBuilder();
		ContextKey context = ContextKey.create();
		LabelNode onFalse = new LabelNode();

		jumpToIfFalse(cb, onFalse, context);
		cb.add(code);
		cb.add(onFalse, context);

		return cb.build();
	}

	public CodePiece doIfFalse(CodePiece code) {
		CodeBuilder cb = new CodeBuilder();
		ContextKey context = ContextKey.create();
		LabelNode onTrue = new LabelNode();

		jumpToIfTrue(cb, onTrue, context);
		cb.add(code);
		cb.add(onTrue, context);

		return cb.build();
	}

	public CodePiece doIfElse(CodePiece onTrue, CodePiece onFalse) {
		if (onTrue.isEmpty()) {
			return doIfFalse(onFalse);
		} else if (onFalse.isEmpty()) {
			return doIfTrue(onTrue);
		}

		CodeBuilder cb = new CodeBuilder();
		ContextKey context = ContextKey.create();

		if (preferJumpOnTrue()) {
			LabelNode trueLbl = new LabelNode();
			LabelNode after = new LabelNode();

			jumpToIfTrue(cb, trueLbl, context);
			cb.add(onFalse);
			cb.add(new JumpInsnNode(GOTO, after), context);

			cb.add(trueLbl, context);
			cb.add(onTrue);
			cb.add(after, context);
		} else {
			LabelNode falseLbl = new LabelNode();
			LabelNode after = new LabelNode();

			jumpToIfFalse(cb, falseLbl, context);
			cb.add(onTrue);
			cb.add(new JumpInsnNode(GOTO, after), context);

			cb.add(falseLbl, context);
			cb.add(onFalse);
			cb.add(after, context);
		}

		return cb.build();
	}

	ASMCondition() { }

	abstract boolean preferJumpOnTrue();

	abstract void jumpToIfTrue(CodeBuilder cb, LabelNode lbl, ContextKey context);
	abstract void jumpToIfFalse(CodeBuilder cb, LabelNode lbl, ContextKey context);

	abstract ASMCondition invokeOrCombination(ASMCondition self);
	abstract ASMCondition invokeAndCombination(ASMCondition self);

	abstract ASMCondition orFromNormal(StandardCondition other);
	abstract ASMCondition orFromOr(OrCondition other);
	abstract ASMCondition orFromAnd(AndCondition other);

	abstract ASMCondition andFromNormal(StandardCondition other);
	abstract ASMCondition andFromOr(OrCondition other);
	abstract ASMCondition andFromAnd(AndCondition other);


}
