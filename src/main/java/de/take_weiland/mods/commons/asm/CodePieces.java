package de.take_weiland.mods.commons.asm;

import de.take_weiland.mods.commons.InstanceProvider;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public final class CodePieces {

	public static CodePiece of() {
		return EmptyCodePiece.INSTANCE;
	}

	public static CodePiece ofOpcode(int opcode) {
		return of(new InsnNode(opcode));
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

	private static AtomicLong cacheCounter;

	private static String nextUniqueName() {
		if (cacheCounter == null) {
			synchronized (CodePieces.class) {
				if (cacheCounter == null) {
					cacheCounter = new AtomicLong();
				}
			}
		}
		return "_sc$cache$" + String.valueOf(cacheCounter.getAndIncrement()).replace('-', 'm');
	}

	public static CodePiece cacheStatic(ClassNode clazz, Type type, CodePiece code) {
		return cache(clazz, type, code, true);
	}

	public static CodePiece cache(ClassNode clazz, Type type, CodePiece code) {
		return cache(clazz, type, code, false);
	}

	public static CodePiece cache(ClassNode clazz, Type type, CodePiece code, boolean _static) {
		CodePiece loadInstance;
		int getOp;
		int putOp;
		int invokeOp;
		int access = ACC_PRIVATE;
		if (_static) {
			loadInstance = of();
			getOp = GETSTATIC;
			putOp = PUTSTATIC;
			invokeOp = INVOKESTATIC;
			access |= ACC_STATIC;
		} else {
			loadInstance = getThis();
			getOp = GETFIELD;
			putOp = PUTFIELD;
			invokeOp = INVOKESPECIAL;
		}

		String name = nextUniqueName();
		String mDesc = Type.getMethodDescriptor(type);
		FieldNode field = new FieldNode(access, name, type.getDescriptor(), null, null);
		MethodNode method = new MethodNode(access, name, mDesc, null, null);

		InsnList insns = method.instructions;
		loadInstance.appendTo(insns);
		insns.add(new FieldInsnNode(getOp, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(DUP));

		LabelNode isNull = new LabelNode();
		insns.add(new JumpInsnNode(IFNULL, isNull));
		insns.add(new InsnNode(type.getOpcode(IRETURN)));

		insns.add(isNull);
		insns.add(new InsnNode(POP));

		loadInstance.appendTo(insns);
		code.appendTo(insns);
		insns.add(new FieldInsnNode(putOp, clazz.name, field.name, field.desc));
		loadInstance.appendTo(insns);
		insns.add(new FieldInsnNode(getOp, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(type.getOpcode(IRETURN)));

		clazz.fields.add(field);
		clazz.methods.add(method);

		InsnList call = new InsnList();
		loadInstance.appendTo(call);
		call.add(new MethodInsnNode(invokeOp, clazz.name, method.name, method.desc));
		return of(call);
	}

	public static CodePiece cacheLocal(ClassNode clazz, MethodNode method, Type type, CodePiece code) {
		int idx = 0;
		for (LocalVariableNode lVar : method.localVariables) {
			if (lVar.index >= idx) idx = lVar.index + 1;
		}
		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();
		method.instructions.insert(start);
		method.instructions.insertBefore(ASMUtils.findLastReturn(method), end);
		LocalVariableNode var = new LocalVariableNode("_sc$local$" + idx, type.getDescriptor(), null, start, end, idx);
		ASMVariable wrap = ASMVariables.of(var);
		wrap.set(code).insertAfter(method.instructions, start);
		return wrap.get();
	}

	public static ASMCondition makeCondition(CodePiece piece) {
		return makeCondition(piece, IFNE, IFEQ);
	}

	public static ASMCondition makeCondition(final CodePiece conditionArgs, final int opcode, final int opcodeNegated) {
		return new ASMCondition() {

			@Override
			public CodePiece ifTrue(CodePiece code) {
				return make(code, opcode);
			}

			@Override
			public CodePiece ifFalse(CodePiece code) {
				return make(code, opcodeNegated);
			}

			@Override
			public CodePiece doIfElse(CodePiece onTrue, CodePiece onFalse) {
				InsnList insns = new InsnList();
				conditionArgs.appendTo(insns);
				LabelNode isTrue = new LabelNode();
				LabelNode after = new LabelNode();
				insns.add(new JumpInsnNode(opcode, isTrue));
				onFalse.appendTo(insns);
				insns.add(new JumpInsnNode(GOTO, after));
				insns.add(isTrue);
				onTrue.appendTo(insns);
				insns.add(after);
				return of(insns);
			}

			private CodePiece make(CodePiece code, int opcode) {
				InsnList insns = new InsnList();
				conditionArgs.appendTo(insns);
				LabelNode after = new LabelNode();
				insns.add(new JumpInsnNode(opcode, after));
				code.appendTo(insns);
				insns.add(after);
				return of(insns);
			}
		};
	}

	public static ASMCondition isNull(final CodePiece value) {
		return makeCondition(value, IFNULL, IFNONNULL);
	}

	public static ASMCondition equal(CodePiece a, CodePiece b, Type type) {
		return equal(a, b, type, false);
	}

	public static ASMCondition equal(CodePiece a, CodePiece b, Type type, boolean useEquals) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return booleansEqual(a, b);
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.CHAR:
				return makeCondition(a.append(b), IF_ICMPEQ, IF_ICMPNE);
			case Type.LONG:
				return makeCondition(a.append(b).append(ofOpcode(LCMP)));
			case Type.FLOAT:
				return makeCondition(a.append(b).append(ofOpcode(FCMPL)));
			case Type.DOUBLE:
				return makeCondition(a.append(b).append(ofOpcode(DCMPL)));
			case Type.OBJECT:
				if (!useEquals) {
					return makeCondition(a.append(b), IF_ACMPEQ, IF_ACMPNE);
				} else {
					Type objectType = Type.getType(Object.class);
					return makeCondition(
							invokeStatic("com/google/common/base/Objects",
									"equal",
									getMethodDescriptor(BOOLEAN_TYPE, objectType, objectType),
									a, b));
				}
			case Type.ARRAY:
				if (!useEquals) {
					return makeCondition(a.append(b), IF_ACMPEQ, IF_ACMPNE);
				} else {
					String mName = type.getDimensions() == 1 ? "equals" : "deepEquals";
					String desc = type.getDimensions() == 1 ?
							getMethodDescriptor(BOOLEAN_TYPE, type, type) :
							getMethodDescriptor(BOOLEAN_TYPE, getType(Object[].class), getType(Object[].class));

					return makeCondition(
							invokeStatic("java/util/Arrays",
									mName,
									desc,
									a, b));
				}
			default:
				throw new IllegalArgumentException("Invalid Type for comparision!");
		}
	}

	private static ASMCondition booleansEqual(final CodePiece a, final CodePiece b) {
		return new ASMCondition() {
			@Override
			public CodePiece ifTrue(CodePiece code) {
				InsnList insns = new InsnList();
				LabelNode aFalse = new LabelNode();
				LabelNode equal = new LabelNode();
				LabelNode after = new LabelNode();
				b.appendTo(insns);
				a.appendTo(insns);
				insns.add(new JumpInsnNode(IFEQ, aFalse)); // if a is false, jump below
				insns.add(new JumpInsnNode(IFNE, after)); // a is true here, if b is false, skip execution
				insns.add(equal);
				code.appendTo(insns);
				insns.add(new JumpInsnNode(GOTO, after));
				insns.add(aFalse);
				insns.add(new JumpInsnNode(IFNE, equal));
				insns.add(after);
				return of(insns);
			}

			@Override
			public CodePiece ifFalse(CodePiece code) {
				InsnList insns = new InsnList();
				LabelNode aFalse = new LabelNode();
				LabelNode notEqual = new LabelNode();
				LabelNode after = new LabelNode();
				b.appendTo(insns);
				a.appendTo(insns);
				insns.add(new JumpInsnNode(IFEQ, aFalse)); // if a is false, jump below
				insns.add(new JumpInsnNode(IFEQ, after)); // a is true here, if b is true, skip execution (condition is false)
				insns.add(notEqual);
				code.appendTo(insns);
				insns.add(new JumpInsnNode(GOTO, after));
				insns.add(aFalse);
				insns.add(new JumpInsnNode(IFEQ, notEqual));
				insns.add(after);
				return of(insns);
			}

			@Override
			public CodePiece doIfElse(CodePiece onTrue, CodePiece onFalse) {
				InsnList insns = new InsnList();
				LabelNode aFalse = new LabelNode();
				LabelNode equal = new LabelNode();
				LabelNode notEqual = new LabelNode();
				LabelNode after = new LabelNode();
				b.appendTo(insns);
				a.appendTo(insns);
				insns.add(new JumpInsnNode(IFEQ, aFalse)); // if a is false, jump below
				insns.add(new JumpInsnNode(IFNE, notEqual)); // a is true here, if b is false, skip execution
				insns.add(equal);
				onTrue.appendTo(insns);
				insns.add(new JumpInsnNode(GOTO, after));
				insns.add(aFalse);
				insns.add(new JumpInsnNode(IFNE, equal));
				insns.add(notEqual);
				onFalse.appendTo(insns);
				insns.add(after);
				return of(insns);
			}
		};
	}

	public static CodePiece invokeStatic(String clazz, String method, String desc, CodePiece... args) {
		checkArgument(args.length == Type.getArgumentTypes(desc).length, "argument count mismatch");
		InsnList insns = new InsnList();
		for (CodePiece arg : args) {
			arg.appendTo(insns);
		}
		insns.add(new MethodInsnNode(INVOKESTATIC, clazz, method, desc));
		return of(insns);
	}

	public static CodePiece invoke(ClassNode clazz, MethodNode method, CodePiece... args) {
		boolean isStatic = (method.access & ACC_STATIC) == ACC_STATIC;
		int reqArgs = ASMUtils.argumentCount(method.desc) + (isStatic ? 0 : 1);
		checkArgument(args.length == reqArgs, "Argument count mismatch");
		InsnList insns = new InsnList();

		for (CodePiece arg : args) {
			arg.appendTo(insns);
		}

		int invokeOp = isStatic ? INVOKESTATIC : (method.access & ACC_PRIVATE) == ACC_PRIVATE ? INVOKESPECIAL : INVOKEVIRTUAL;
		insns.add(new MethodInsnNode(invokeOp, clazz.name, method.name, method.desc));
		return of(insns);
	}

	public static CodePiece instantiate(Class<?> c) {
		return instantiate(Type.getInternalName(c));
	}

	public static CodePiece instantiate(Type t) {
		return instantiate(t.getInternalName());
	}

	public static CodePiece instantiate(String internalName) {
		return instantiate(internalName, new Type[0]);
	}

	public static CodePiece instantiate(Class<?> c, Type[] argTypes, CodePiece... args) {
		return instantiate(Type.getInternalName(c), argTypes, args);
	}

	public static CodePiece instantiate(Type t, Type[] argTypes, CodePiece... args) {
		return instantiate(t.getInternalName(), argTypes, args);
	}

	public static CodePiece instantiate(String internalName, Type[] argTypes, CodePiece... args) {
		checkArgument(args.length == argTypes.length, "parameter list length mismatch");
		InsnList insns = new InsnList();
		insns.add(new TypeInsnNode(NEW, internalName));
		insns.add(new InsnNode(DUP));
		for (CodePiece arg : args) {
			arg.appendTo(insns);
		}
		insns.add(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", getMethodDescriptor(VOID_TYPE, argTypes)));
		return of(insns);
	}

	public static CodePiece obtainInstance(ClassNode callingClass, ClassNode targetClass) {
		return obtainInstance(callingClass, targetClass, new Type[0]);
	}

	public static CodePiece obtainInstance(ClassNode callingClass, ClassNode targetClass, Type[] argTypes, CodePiece... args) {
		Type targetType = Type.getObjectType(targetClass.name);
		String desc = Type.getMethodDescriptor(targetType, argTypes);
		Collection<MethodNode> methods = ASMUtils.methodsWith(targetClass, InstanceProvider.class);
		for (MethodNode method : methods) {
			if (method.desc.equals(desc)) {
				if ((method.access & ACC_STATIC) != ACC_STATIC) {
					throw new IllegalStateException(String.format("@InstanceProvider present on non-static method %s in %s", method.name, targetClass.name));
				}
				return invoke(targetClass, method, args);
			}
		}

		for (MethodNode cstr : ASMUtils.getConstructors(targetClass)) {
			if (cstr.desc.equals(desc)) {
				return instantiate(targetType.getInternalName(), argTypes, args);
			}
		}
		return null;
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

	private static CodePiece thisLoader;
	public static CodePiece getThis() {
		return thisLoader == null ? (thisLoader = of(new VarInsnNode(ALOAD, 0))) : thisLoader;
	}

	private static CodePiece nullLoader;
	public static CodePiece constantNull() {
		return nullLoader == null ? (nullLoader = of(new InsnNode(ACONST_NULL))) : nullLoader;
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
