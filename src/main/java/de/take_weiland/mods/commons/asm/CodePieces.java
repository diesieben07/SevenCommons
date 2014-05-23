package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Array;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * @author diesieben07
 */
public final class CodePieces {

	public static CodePiece of() {
		return EmptyCodePiece.INSTANCE;
	}

	public static CodePiece of(AbstractInsnNode insn) {
		return new SingleInsnCodePiece(insn);
	}

	public static CodePiece of(InsnList insns) {
		int size = insns.size();
		if (size == 0) {
			return EmptyCodePiece.INSTANCE;
		} else if (size == 1) {
			return of(insns.getFirst());
		} else {
			return new InsnListCodePiece(insns);
		}
	}

	public static CodePiece instantiate(Class<?> c) {
		return instantiate(Type.getInternalName(c));
	}

	public static CodePiece instantiate(Type t) {
		return instantiate(t.getInternalName());
	}

	public static CodePiece instantiate(String internalName) {
		InsnList insns = new InsnList();
		insns.add(new TypeInsnNode(NEW, internalName));
		insns.add(new InsnNode(DUP));
		insns.add(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", getMethodDescriptor(VOID_TYPE)));
		return of(insns);
	}

	public static CodePiece castTo(Class<?> c) {
		return castTo(Type.getInternalName(c));
	}

	public static CodePiece castTo(Type type) {
		return castTo(type.getInternalName());
	}

	public static CodePiece castTo(String internalName) {
		return of(new TypeInsnNode(CHECKCAST, internalName));
	}

	public static CodePiece getThis() {
		return of(new VarInsnNode(ALOAD, 0));
	}

	public static CodePiece constantNull() {
		return of(new InsnNode(ACONST_NULL));
	}

	public static CodePiece constant(Object o) {
		if (o == null) {
			return constantNull();
		} else if (o instanceof Boolean) {
			return constant(((boolean) o));
		} else if (o instanceof Byte) {
			return constant(((byte) o));
		} else if (o instanceof Short) {
			return constant(((short) o));
		} else if (o instanceof Integer) {
			return constant(((int) o));
		} else if (o instanceof Long || o instanceof Float || o instanceof Double || o instanceof String || o instanceof Type) {
			return ofLdc(o);
		} else if (o instanceof Class) {
			return constant(((Class<?>) o));
		} else if (o.getClass().isArray()) {
			return ofArray(o);
		}
		throw new IllegalArgumentException("Invalid constant: " + o);
	}

	public static CodePiece constant(boolean b) {
		return constant(b ? 1 : 0);
	}

	public static CodePiece constant(byte b) {
		return constant((int) b);
	}

	public static CodePiece constant(short s) {
		return constant((int) s);
	}

	public static CodePiece constant(int i) {
		return of(loadInt(i));
	}

	public static CodePiece constant(Long l) {
		return ofLdc(l);
	}

	public static CodePiece constant(Float f) {
		return ofLdc(f);
	}

	public static CodePiece constant(Double d) {
		return ofLdc(d);
	}

	public static CodePiece constant(String s) {
		return ofLdc(s);
	}

	public static CodePiece constant(Type t) {
		return ofLdc(t);
	}

	public static CodePiece constant(Class<?> c) {
		return c == null ? constantNull() : ofLdc(Type.getType(c));
	}

	public static CodePiece constant(boolean[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(byte[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(short[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(int[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(long[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(char[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(float[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(double[] arr) {
		return ofArray(arr);
	}

	public static CodePiece constant(Object[] arr) {
		return ofArray(arr);
	}

	private static CodePiece ofArray(Object arr) {
		InsnList insns = new InsnList();
		Type compType = Type.getType(arr.getClass().getComponentType());

		int len = Array.getLength(arr);

		insns.add(loadInt(len));

		if (ASMUtils.isPrimitive(compType)) {
			insns.add(new IntInsnNode(NEWARRAY, toArrayType(compType)));
		} else {
			insns.add(new TypeInsnNode(ANEWARRAY, compType.getInternalName()));
		}

		int storeOpcode = compType.getOpcode(IASTORE);

		for (int i = 0; i < len; ++i) {
			insns.add(new InsnNode(DUP));
			insns.add(loadInt(i));
			constant(Array.get(arr, i));
			insns.add(new InsnNode(storeOpcode));
		}

		return of(insns);
	}

	private static int toArrayType(Type type) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return T_BOOLEAN;
			case Type.BYTE:
				return T_BYTE;
			case Type.SHORT:
				return T_SHORT;
			case Type.INT:
				return T_INT;
			case Type.LONG:
				return T_LONG;
			case Type.CHAR:
				return T_CHAR;
			case Type.FLOAT:
				return T_FLOAT;
			case Type.DOUBLE:
				return T_DOUBLE;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static AbstractInsnNode loadInt(int i) {
		if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
			return new IntInsnNode(BIPUSH, i);
		} else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
			return new IntInsnNode(SIPUSH, i);
		} else {
			return new LdcInsnNode(i);
		}
	}

	private static CodePiece ofLdc(Object o) {
		if (o == null) {
			return constantNull();
		} else {
			return of(new LdcInsnNode(o));
		}
	}

}
