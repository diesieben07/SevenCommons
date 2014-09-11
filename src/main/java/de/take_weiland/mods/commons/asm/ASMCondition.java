package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public abstract class ASMCondition {

	/**
	 * <p>Create an ASMCondition that checks if the two values are the same.</p>
	 * <p>This resembles == comparison.</p>
	 * @param a the first value
	 * @param b the second value
	 * @param type the common supertype of the values
	 * @return an ASMCondition
	 */
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

	/**
	 * <p>Create an ASMCondition that checks if the two values are equal.</p>
	 * <p>For primitives == comparison is used. For Objects a null-guarded check to {@link Object#equals(Object)} is used. Arrays of single-dimension use the appropriate version of
	 * {@link java.util.Arrays#equals(Object[], Object[])}, multi-dimensional arrays use a call to {@link java.util.Arrays#deepEquals(Object[], Object[])}.</p>
	 * @param a the first value
	 * @param b the second value
	 * @param type the common supertype of the values
	 * @return an ASMCondition
	 */
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

	/**
	 * <p>Create an ASMCondition that checks whether the value is {@code null}.</p>
	 * @param value the value
	 * @return an ASMCondition
	 */
	public static ASMCondition ifNull(CodePiece value) {
		return new StandardCondition(value, IFNULL);
	}

	/**
	 * <p>Create an ASMCondition that checks whether the value is not {@code null}.</p>
	 * @param value the value
	 * @return an ASMCondition
	 */
	public static ASMCondition ifNotNull(CodePiece value) {
		return new StandardCondition(value, IFNONNULL);
	}

	/**
	 * <p>Create an ASMCondition that checks whether the boolean value is {@code true}.</p>
	 * @param booleanValue the boolean value
	 * @return an ASMCondition
	 */
	public static ASMCondition ifTrue(CodePiece booleanValue) {
		return new StandardCondition(booleanValue, IFNE);
	}

	/**
	 * <p>Create an ASMCondition that checks whether the boolean value is {@code true}.</p>
	 * @param booleanValue the boolean value
	 * @return an ASMCondition
	 */
	public static ASMCondition ifFalse(CodePiece booleanValue) {
		return new StandardCondition(booleanValue, IFEQ);
	}

	/**
	 * <p>Create an ASMCondition that compares the given arguments using the given opcode.</p>
	 * <p>The opcode must be one of IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE,
	 * IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, IFNULL or IFNONNULL.</p>
	 * @param cmpOpcode the opcode
	 * @param args the arguments
	 * @return an ASMCondition
	 */
	public static ASMCondition custom(int cmpOpcode, CodePiece... args) {
		negateJmpOpcode(cmpOpcode); // for error check
		return new StandardCondition(CodePieces.concat(args), cmpOpcode);
	}

	/**
	 * <p>Create an ASMCondition that evaluates to true if any of the given conditions evaluate to true.</p>
	 * <p>The conditions are evaluated in order and the evaluation short-circuits if any of the conditions evaluate to true.</p>
	 * @param conditions the conditions
	 * @return an ASMCondition
	 */
	public static ASMCondition or(@NotNull ASMCondition... conditions) {
		List<ASMCondition> all = Lists.newArrayListWithExpectedSize(conditions.length);
		for (ASMCondition condition : conditions) {
			condition.unwrapIntoOr(all);
		}
		return new OrCondition(all.toArray(new ASMCondition[all.size()]));
	}

	/**
	 * <p>Create an ASMCondition that evaluates to true if all of the given conditions evaluate to true.</p>
	 * <p>The conditions are evaluated in order and the evaluation short-circuits if any of the conditions evaluate to false.</p>
	 * @param conditions the conditions
	 * @return an ASMCondition
	 */
	public static ASMCondition and(@NotNull ASMCondition... conditions) {
		List<ASMCondition> all = Lists.newArrayListWithCapacity(conditions.length);
		for (ASMCondition condition : conditions) {
			condition.unwrapIntoAnd(all);
		}
		return new AndCondition(all.toArray(new ASMCondition[all.size()]));
	}

	/**
	 * <p>Create a new ASMCondition that is true if either this or the other ASMCondition evaluate to true.</p>
	 * @param other the other ASMCondition
	 * @return an ASMCondition
	 */
	public ASMCondition or(ASMCondition other) {
		return or(this, other);
	}

	/**
	 * <p>Create a new ASMCondition that is true if both this and the other ASMCondition evaluate to true.</p>
	 * @param other the other ASMCondition
	 * @return an ASMCondition
	 */
	public ASMCondition and(ASMCondition other) {
		return and(this, other);
	}

	/**
	 * <p>Create a new ASMCondition that is the negation of this ASMCondition.</p>
	 * @return an ASMCondition
	 */
	public abstract ASMCondition negate();

	/**
	 * <p>Create a CodePiece that only executes {@code code} if this condition evaluates to true.</p>
	 * @param code the code to execute
	 * @return a CodePiece
	 */
	public CodePiece doIfTrue(CodePiece code) {
		CodeBuilder cb = new CodeBuilder();
		ContextKey context = ContextKey.create();
		LabelNode onFalse = new LabelNode();

		jumpToIfFalse(cb, onFalse, context);
		cb.add(code);
		cb.add(onFalse, context);

		return cb.build();
	}

	/**
	 * <p>Create a CodePiece that only executes {@code code} if this condition evaluates to false.</p>
	 * @param code the code to execute
	 * @return a CodePiece
	 */
	public CodePiece doIfFalse(CodePiece code) {
		CodeBuilder cb = new CodeBuilder();
		ContextKey context = ContextKey.create();
		LabelNode onTrue = new LabelNode();

		jumpToIfTrue(cb, onTrue, context);
		cb.add(code);
		cb.add(onTrue, context);

		return cb.build();
	}

	/**
	 * <p>Create a CodePiece that executes {@code onTrue} if this condition evaluates to true and {@code onFalse} otherwise.</p>
	 * @param onTrue the code to execute on true
	 * @param onFalse the code to execute on false
	 * @return a CodePiece
	 */
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

	abstract void unwrapIntoOr(List<ASMCondition> list);
	abstract void unwrapIntoAnd(List<ASMCondition> list);

	static int negateJmpOpcode(int op) {
		switch (op) {
			case IFEQ:
				return IFNE;
			case IFNE:
				return IFEQ;

			case IF_ACMPEQ:
				return IF_ACMPNE;
			case IF_ACMPNE:
				return IF_ACMPEQ;

			case IF_ICMPEQ:
				return IF_ICMPNE;
			case IF_ICMPNE:
				return IF_ICMPEQ;

			case IFNULL:
				return IFNONNULL;
			case IFNONNULL:
				return IFNULL;

			case IFLT:
				return IFGE;
			case IFGE:
				return IFLT;

			case IFLE:
				return IFGT;
			case IFGT:
				return IFLE;

			case IF_ICMPLT:
				return IF_ICMPGE;
			case IF_ICMPGE:
				return IF_ICMPLT;

			case IF_ICMPGT:
				return IF_ICMPLE;
			case IF_ICMPLE:
				return IF_ICMPGT;

			default:
				throw new IllegalArgumentException("No IF-Opcode");
		}
	}
}
