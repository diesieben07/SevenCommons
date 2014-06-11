package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Iterators;
import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.InstanceProvider;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
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

	public static CodePiece[] parse(Object... data) {
		int len = data.length;
		CodePiece[] result = new CodePiece[len];
		for (int i = 0; i < len; ++i) {
			result[i] = parse0(data[i]);
		}
		return result;
	}

	private static CodePiece parse0(Object o) {
		if (o instanceof CodePiece) {
			return (CodePiece) o;
		} else if (o instanceof AbstractInsnNode) {
			return of((AbstractInsnNode) o);
		} else if (o instanceof InsnList) {
			return of((InsnList) o);
		} else {
			return constant(o);
		}
	}

	public static CodePiece concat(CodePiece... pieces) {
		if (pieces.length == 0) {
			return of();
		} else {
			return new CombinedCodePiece(pieces);
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
		return cache(clazz, type, code, true, false);
	}

	public static CodePiece cache(ClassNode clazz, Type type, CodePiece code) {
		return cache(clazz, type, code, false, false);
	}

	public static CodePiece cache(ClassNode clazz, Type type, CodePiece code, boolean _static, boolean canBeNull) {
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

		boolean isPrimitive = ASMUtils.isPrimitive(type);

		String name = nextUniqueName();
		String mDesc = Type.getMethodDescriptor(type);
		FieldNode field = new FieldNode(access, name, type.getDescriptor(), null, null);
		FieldNode marker = null;
		if (canBeNull || isPrimitive) {
			marker = new FieldNode(access, name + "$present", Type.BOOLEAN_TYPE.getDescriptor(), null, null);
			clazz.fields.add(marker);
		}

		MethodNode method = new MethodNode(access, name, mDesc, null, null);
		InsnList insns = method.instructions;
		if (marker == null) {
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
		} else {
			loadInstance.appendTo(insns);
			insns.add(new FieldInsnNode(getOp, clazz.name, marker.name, marker.desc));

			LabelNode isMissing = new LabelNode();
			insns.add(new JumpInsnNode(IFEQ, isMissing));
			loadInstance.appendTo(insns);
			insns.add(new FieldInsnNode(getOp, clazz.name, field.name, field.desc));
			insns.add(new InsnNode(type.getOpcode(IRETURN)));

			insns.add(isMissing);
			loadInstance.appendTo(insns);
			constant(true).appendTo(insns);
			insns.add(new FieldInsnNode(putOp, clazz.name, marker.name, marker.desc));

			loadInstance.appendTo(insns);
			code.appendTo(insns);
			insns.add(new FieldInsnNode(putOp, clazz.name, field.name, field.desc));

			loadInstance.appendTo(insns);
			insns.add(new FieldInsnNode(getOp, clazz.name, field.name, field.desc));

			insns.add(new InsnNode(type.getOpcode(IRETURN)));
		}

		clazz.fields.add(field);
		clazz.methods.add(method);

		return invoke(invokeOp, clazz.name, method.name, method.desc, loadInstance);
	}

	public static LocalCache cacheLocal(MethodNode method, Type type, CodePiece code) {
		int idx;
		if ((method.access & ACC_STATIC) == ACC_STATIC) {
			idx = 0;
		} else {
			idx = 1;
		}
		for (LocalVariableNode var : method.localVariables) {
			if (var.index >= idx) idx = var.index + 1;
		}
		Iterator<VarInsnNode> varInsns = Iterators.filter(method.instructions.iterator(), VarInsnNode.class);
		while (varInsns.hasNext()) {
			VarInsnNode insn = varInsns.next();
			if (insn.var >= idx) idx = insn.var + 1;
		}

		return cacheLocal(method, type, code, idx);
	}

	public static LocalCache cacheLocal(MethodNode method, Type type, CodePiece code, int varIndex) {
		LocalVariableNode var = new LocalVariableNode(null, type.getDescriptor(), null, null, null, varIndex);
		ASMVariable theCache = ASMVariables.of(var);
		theCache.set(constantNull()).prependTo(method.instructions);

		return new LocalCache(Conditions.ifNull(theCache.get())
				.then(theCache.set(code))
				.build()
				.append(theCache.get()), theCache.get());
	}

	public static final class LocalCache {

		public final CodePiece get;
		public final CodePiece direct;

		LocalCache(CodePiece get, CodePiece direct) {
			this.get = get;
			this.direct = direct;
		}
	}

	public static CodePiece getField(ClassNode clazz, FieldNode field, CodePiece instance) {
		checkArgument((field.access & ACC_STATIC) != ACC_STATIC, "No instance needed for static field");
		return instance.append(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
	}

	public static CodePiece getField(ClassNode clazz, FieldNode field) {
		checkArgument((field.access & ACC_STATIC) == ACC_STATIC, "Instance needed for non-static field");
		return of(new FieldInsnNode(GETSTATIC, clazz.name, field.name, field.desc));
	}

	public static CodePiece getField(String clazz, String field, Type type, CodePiece instance) {
		return getField(clazz, field, type.getDescriptor(), instance);
	}

	public static CodePiece getField(String clazz, String field, Class<?> type, CodePiece instance) {
		return getField(clazz, field, Type.getDescriptor(type), instance);
	}

	public static CodePiece getField(String clazz, String field, String desc, CodePiece instance) {
		return instance.append(new FieldInsnNode(GETFIELD, clazz, field, desc));
	}

	public static CodePiece getField(String clazz, String field, Type type) {
		return getField(clazz, field, type.getDescriptor());
	}

	public static CodePiece getField(String clazz, String field, Class<?> type) {
		return getField(clazz, field, Type.getDescriptor(type));
	}

	public static CodePiece getField(String clazz, String field, String desc) {
		return of(new FieldInsnNode(GETSTATIC, clazz, field, desc));
	}

	public static CodePiece invokeStatic(String clazz, String method, String desc, CodePiece... args) {
		checkArgument(args.length == Type.getArgumentTypes(desc).length, "argument count mismatch");
		return invoke(INVOKESTATIC, clazz, method, desc, args);
	}

	public static CodePiece invoke(int invokeOpcode, String clazz, String method, String desc, CodePiece... args) {
		boolean isStatic = invokeOpcode == INVOKESTATIC;
		int reqArgs = ASMUtils.argumentCount(desc) + (isStatic ? 0 : 1);
		checkArgument(args.length == reqArgs, "Argument count mismatch");

		return concat(args).append(new MethodInsnNode(invokeOpcode, clazz, method, desc));
	}

	public static CodePiece invoke(ClassNode clazz, MethodNode method, CodePiece... args) {
		boolean isStatic = (method.access & ACC_STATIC) == ACC_STATIC;
		boolean isPrivate = (method.access & ACC_PRIVATE) == ACC_PRIVATE;
		boolean isInterface = (clazz.access & ACC_INTERFACE) == ACC_INTERFACE;
		int opcode = isStatic ? INVOKESTATIC : (isPrivate ? INVOKESPECIAL : (isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL));
		return invoke(opcode, clazz.name, method.name, method.desc, args);
	}

	public static CodePiece invokeSuper(ClassNode clazz, MethodNode method, CodePiece... args) {
		checkArgument((method.access & ACC_STATIC) != ACC_STATIC, "Cannot call super on static method");
		checkArgument((method.access & ACC_PRIVATE) != ACC_PRIVATE, "Cannot call super on private method");
		return invoke(INVOKESPECIAL, clazz.superName, method.name, method.desc, ObjectArrays.concat(getThis(), args));
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
		return of(new TypeInsnNode(NEW, internalName))
				.append(new InsnNode(DUP))
				.append(concat(args))
				.append(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", getMethodDescriptor(VOID_TYPE, argTypes)));
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

		desc = Type.getMethodDescriptor(VOID_TYPE, argTypes);
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

	public static CodePiece castTo(Class<?> c, CodePiece code) {
		return castTo(Type.getInternalName(c), code);
	}

	public static CodePiece castTo(Type type, CodePiece code) {
		return castTo(type.getInternalName(), code);
	}

	public static CodePiece castTo(String internalName, CodePiece code) {
		return code.append(new TypeInsnNode(CHECKCAST, internalName));
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
		} else if (o instanceof Enum) {
			return constant((Enum<?>) o);
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

	public static CodePiece constant(long l) {
		if (l == 0) {
			return ofOpcode(LCONST_0);
		} else if (l == 1) {
			return ofOpcode(LCONST_1);
		} else {
			return ofLdc(l);
		}
	}

	public static CodePiece constant(float f) {
		if (f == 0f) {
			return ofOpcode(FCONST_0);
		} else if (f == 1f) {
			return ofOpcode(FCONST_1);
		} else if (f == 2f) {
			return ofOpcode(FCONST_2);
		} else {
			return ofLdc(f);
		}
	}

	public static CodePiece constant(double d) {
		if (d == 0d) {
			return ofOpcode(DCONST_0);
		} else if (d == 1d) {
			return ofOpcode(DCONST_1);
		} else {
			return ofLdc(d);
		}
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

	public static CodePiece constant(Enum<?> e) {
		Class<? extends Enum<?>> enumClass = e.getDeclaringClass();
		return getField(Type.getInternalName(enumClass), e.name(), enumClass);
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
		Type compType = Type.getType(arr.getClass().getComponentType());

		int len = Array.getLength(arr);

		CodeBuilder builder = new CodeBuilder();
		builder.add(loadInt(len));

		if (ASMUtils.isPrimitive(compType)) {
			builder.add(new IntInsnNode(NEWARRAY, toArrayType(compType)));
		} else {
			builder.add(new TypeInsnNode(ANEWARRAY, compType.getInternalName()));
		}

		int storeOpcode = compType.getOpcode(IASTORE);

		for (int i = 0; i < len; ++i) {
			builder.add(new InsnNode(DUP))
					.add(loadInt(i))
					.add(constant(Array.get(arr, i)))
					.add(new InsnNode(storeOpcode));
		}

		return builder.build();
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
		if (i >= -1 && i <= 5) {
			switch (i) {
				case -1:
					return new InsnNode(ICONST_M1);
				case 0:
					return new InsnNode(ICONST_0);
				case 1:
					return new InsnNode(ICONST_1);
				case 2:
					return new InsnNode(ICONST_2);
				case 3:
					return new InsnNode(ICONST_3);
				case 4:
					return new InsnNode(ICONST_4);
				case 5:
					return new InsnNode(ICONST_5);
				default:
					throw new AssertionError();
			}
		}
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
