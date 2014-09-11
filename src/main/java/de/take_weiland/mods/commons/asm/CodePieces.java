package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkArgument;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * <p>Factory class for CodePieces</p>
 *
 * @author diesieben07
 * @see de.take_weiland.mods.commons.asm.CodePiece
 */
public final class CodePieces {

	/**
	 * <p>Creates an empty CodePiece.</p>
	 *
	 * @return an empty CodePiece
	 */
	public static CodePiece of() {
		return EmptyCodePiece.INSTANCE;
	}

	/**
	 * <p>Creates a CodePiece that represents a single opcode. The opcode must be valid for an {@link org.objectweb.asm.tree.InsnNode} (it takes no parameters).</p>
	 *
	 * @param opcode the opcode
	 * @return a CodePiece that represents the opcode
	 */
	public static CodePiece ofOpcode(int opcode) {
		return of(new InsnNode(opcode));
	}

	/**
	 * <p>Creates a CodePiece that represents the given instruction.</p>
	 *
	 * @param insn the instruction
	 * @return a CodePiece
	 */
	public static CodePiece of(AbstractInsnNode insn) {
		return new SingleInsnCodePiece(insn);
	}

	/**
	 * <p>Creates a CodePiece that represents all instructions in the given list. The list should not be used after being passed to this method.</p>
	 *
	 * @param insns the InsnList
	 * @return a CodePiece
	 */
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

	/**
	 * <p>Create a CodePiece that represents all CodePieces in the given array, in order.</p>
	 *
	 * @param pieces the pieces to concatenate
	 * @return a CodePiece
	 */
	public static CodePiece concat(CodePiece... pieces) {
		switch (pieces.length) {
			case 0:
				return of();
			case 1:
				return pieces[0];
			case 2:
				return pieces[0].append(pieces[1]);
			default:
				for (CodePiece piece : pieces) {
					if (piece.isCombined()) {
						// flatten hierarchy
						ArrayList<CodePiece> all = Lists.newArrayList();
						for (CodePiece codePiece : pieces) {
							codePiece.unwrapInto(all);
						}
						return new CombinedCodePiece(all.toArray(new CodePiece[all.size()]));
					}
				}
				return new CombinedCodePiece(pieces);

		}
	}

	/**
	 * <p>Create a CodePiece that will get the given field from the given class using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the field
	 * @param instance a CodePiece representing the instance
	 * @return a CodePiece
	 */
	public static CodePiece getField(ClassNode clazz, FieldNode field, CodePiece instance) {
		requireNotStatic(field);
		return instance.append(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
	}

	/**
	 * <p>Create a CodePiece that will get the given static field from the given class.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the field
	 * @return a CodePiece
	 */
	public static CodePiece getField(ClassNode clazz, FieldNode field) {
		requireStatic(field);
		return of(new FieldInsnNode(GETSTATIC, clazz.name, field.name, field.desc));
	}

	private static void requireNotStatic(FieldNode field) {
		checkArgument((field.access & ACC_STATIC) != ACC_STATIC, "No instance needed for static field");
	}

	private static void requireStatic(FieldNode field) {
		checkArgument((field.access & ACC_STATIC) == ACC_STATIC, "Instance needed for non-static field");
	}

	/**
	 * <p>Create a CodePiece that will get the given field from the given class using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the name of the field
	 * @param type     the type of the field
	 * @param instance a CodePiece representing the instance
	 * @return a CodePiece
	 */
	public static CodePiece getField(String clazz, String field, Type type, CodePiece instance) {
		return getField(clazz, field, type.getDescriptor(), instance);
	}

	/**
	 * <p>Create a CodePiece that will get the given field from the given class using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the name of the field
	 * @param type     the type of the field
	 * @param instance a CodePiece representing the instance
	 * @return a CodePiece
	 */
	public static CodePiece getField(String clazz, String field, Class<?> type, CodePiece instance) {
		return getField(clazz, field, Type.getDescriptor(type), instance);
	}

	/**
	 * <p>Create a CodePiece that will get the given field from the given class using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the name of the field
	 * @param desc     the type descriptor of the field
	 * @param instance a CodePiece representing the instance
	 * @return a CodePiece
	 */
	public static CodePiece getField(String clazz, String field, String desc, CodePiece instance) {
		return instance.append(new FieldInsnNode(GETFIELD, clazz, field, desc));
	}

	/**
	 * <p>Create a CodePiece that will get the given static field from the given class.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the field
	 * @param type  the type of the field
	 * @return a CodePiece
	 */
	public static CodePiece getField(String clazz, String field, Type type) {
		return getField(clazz, field, type.getDescriptor());
	}

	/**
	 * <p>Create a CodePiece that will get the given static field from the given class.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the field
	 * @param type  the type of the field
	 * @return a CodePiece
	 */
	public static CodePiece getField(String clazz, String field, Class<?> type) {
		return getField(clazz, field, Type.getDescriptor(type));
	}

	/**
	 * <p>Create a CodePiece that will get the given static field from the given class.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the field
	 * @param desc  the type descriptor of the field
	 * @return a CodePiece
	 */
	public static CodePiece getField(String clazz, String field, String desc) {
		return of(new FieldInsnNode(GETSTATIC, clazz, field, desc));
	}

	/**
	 * <p>Create a CodePiece that will set the given field to the given value using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the field
	 * @param instance a CodePiece representing the instance
	 * @param value    a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(ClassNode clazz, FieldNode field, CodePiece instance, CodePiece value) {
		requireNotStatic(field);
		return setField(clazz.name, field.name, field.desc, instance, value);
	}

	/**
	 * <p>Create a CodePiece that will set the given static field to the given value.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the field
	 * @param value a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(ClassNode clazz, FieldNode field, CodePiece value) {
		requireStatic(field);
		return setField(clazz.name, field.name, field.desc, value);
	}

	/**
	 * <p>Create a CodePiece that will set the given field to the given value using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the name of the field
	 * @param type     the type of the field
	 * @param instance a CodePiece representing the instance
	 * @param value    a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(String clazz, String field, Type type, CodePiece instance, CodePiece value) {
		return setField(clazz, field, type.getDescriptor(), instance, value);
	}

	/**
	 * <p>Create a CodePiece that will set the given field to the given value using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the name of the field
	 * @param type     the type of the field
	 * @param instance a CodePiece representing the instance
	 * @param value    a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(String clazz, String field, Class<?> type, CodePiece instance, CodePiece value) {
		return setField(clazz, field, getDescriptor(type), instance, value);
	}

	/**
	 * <p>Create a CodePiece that will set the given field to the given value using the given instance.</p>
	 *
	 * @param clazz    the class containing the field
	 * @param field    the name of the field
	 * @param desc     the type descriptor of the field
	 * @param instance a CodePiece representing the instance
	 * @param value    a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(String clazz, String field, String desc, CodePiece instance, CodePiece value) {
		return instance.append(value).append(of(new FieldInsnNode(PUTFIELD, clazz, field, desc)));
	}

	/**
	 * <p>Create a CodePiece that will set the given static field to the given value.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the name of the field
	 * @param type  the type of the field
	 * @param value a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(String clazz, String field, Type type, CodePiece value) {
		return setField(clazz, field, type.getDescriptor(), value);
	}

	/**
	 * <p>Create a CodePiece that will set the given static field to the given value.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the name of the field
	 * @param type  the type of the field
	 * @param value a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(String clazz, String field, Class<?> type, CodePiece value) {
		return setField(clazz, field, getDescriptor(type), value);
	}

	/**
	 * <p>Create a CodePiece that will set the given static field to the given value.</p>
	 *
	 * @param clazz the class containing the field
	 * @param field the name of the field
	 * @param desc  the type descriptor of the field
	 * @param value a CodePiece representing the new value
	 * @return a CodePiece
	 */
	public static CodePiece setField(String clazz, String field, String desc, CodePiece value) {
		return value.append(of(new FieldInsnNode(PUTSTATIC, clazz, field, desc)));
	}

	/**
	 * <p>Create a CodePiece that will invoke the given static method with the given arguments.</p>
	 *
	 * @param clazz  the class containing the method
	 * @param method the method to invoke
	 * @param desc   the method descriptor
	 * @param args   the arguments for the method
	 * @return a CodePiece
	 */
	public static CodePiece invokeStatic(String clazz, String method, String desc, CodePiece... args) {
		checkArgument(args.length == ASMUtils.argumentCount(desc), "argument count mismatch");
		return invoke(INVOKESTATIC, clazz, method, desc, args);
	}

	/**
	 * <p>Create a CodePiece that will invoke the given method using the given opcode and arguments.</p>
	 *
	 * @param invokeOpcode the opcode to use to invoke the method (must be {@code INVOKEVIRTUAL}, {@code INVOKESPECIAL} or {@code INVOKESTATIC})
	 * @param clazz        the class containing the method
	 * @param method       the method to invoke
	 * @param desc         the method descriptor
	 * @param args         the arguments for the method (in case of a non-static method pass the instance as the first argument)
	 * @return a CodePiece
	 */
	public static CodePiece invoke(int invokeOpcode, String clazz, String method, String desc, CodePiece... args) {
		boolean isStatic = invokeOpcode == INVOKESTATIC;
		int reqArgs = ASMUtils.argumentCount(desc) + (isStatic ? 0 : 1);
		checkArgument(args.length == reqArgs, "Argument count mismatch");

		return concat(args).append(new MethodInsnNode(invokeOpcode, clazz, method, desc));
	}

	/**
	 * <p>Create a CodePiece that will invoke the given method using the given opcode and arguments.</p>
	 *
	 * @param clazz  the class containing the method
	 * @param method the method to invoke
	 * @param args   the arguments for the method (in case of a non-static method pass the instance as the first argument)
	 * @return a CodePiece
	 */
	public static CodePiece invoke(ClassNode clazz, MethodNode method, CodePiece... args) {
		boolean isStatic = (method.access & ACC_STATIC) == ACC_STATIC;
		boolean isPrivate = (method.access & ACC_PRIVATE) == ACC_PRIVATE;
		boolean isInterface = (clazz.access & ACC_INTERFACE) == ACC_INTERFACE;
		int opcode = isStatic ? INVOKESTATIC : (isPrivate ? INVOKESPECIAL : (isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL));
		return invoke(opcode, clazz.name, method.name, method.desc, args);
	}

	/**
	 * <p>Create a CodePiece that will invoke the super method of the given method using the given opcode and arguments.</p>
	 *
	 * @param clazz  the class containing the method (not the super method)
	 * @param method the method to invoke
	 * @param args   the arguments for the method
	 * @return a CodePiece
	 */
	public static CodePiece invokeSuper(ClassNode clazz, MethodNode method, CodePiece... args) {
		checkArgument((method.access & ACC_STATIC) != ACC_STATIC, "Cannot call super on static method");
		checkArgument((method.access & ACC_PRIVATE) != ACC_PRIVATE, "Cannot call super on private method");
		return invoke(INVOKESPECIAL, clazz.superName, method.name, method.desc, ObjectArrays.concat(getThis(), args));
	}

	/**
	 * <p>Create a CodePiece that will create a new instance of the given class.</p>
	 *
	 * @param c the class to instantiate
	 * @return a CodePiece
	 */
	public static CodePiece instantiate(Class<?> c) {
		return instantiate(Type.getInternalName(c));
	}

	/**
	 * <p>Create a CodePiece that will create a new instance of the given class.</p>
	 *
	 * @param t the class to instantiate
	 * @return a CodePiece
	 */
	public static CodePiece instantiate(Type t) {
		return instantiate(t.getInternalName());
	}

	/**
	 * <p>Create a CodePiece that will create a new instance of the given class.</p>
	 *
	 * @param internalName the internal name of the class to instantiate
	 * @return a CodePiece
	 */
	public static CodePiece instantiate(String internalName) {
		return instantiate(internalName, new Type[0]);
	}

	/**
	 * <p>Create a CodePiece that will create a new instance of the given class, passing the given arguments to the constructor.</p>
	 *
	 * @param c        the class to instantiate
	 * @param argTypes the constructor argument types, in order
	 * @param args     the constructor arguments
	 * @return a CodePiece
	 */
	public static CodePiece instantiate(Class<?> c, Type[] argTypes, CodePiece... args) {
		return instantiate(Type.getInternalName(c), argTypes, args);
	}

	/**
	 * <p>Create a CodePiece that will create a new instance of the given class, passing the given arguments to the constructor.</p>
	 *
	 * @param t        the class to instantiate
	 * @param argTypes the constructor argument types, in order
	 * @param args     the constructor arguments
	 * @return a CodePiece
	 */
	public static CodePiece instantiate(Type t, Type[] argTypes, CodePiece... args) {
		return instantiate(t.getInternalName(), argTypes, args);
	}

	/**
	 * <p>Create a CodePiece that will create a new instance of the given class, passing the given arguments to the constructor.</p>
	 *
	 * @param internalName the internal name of the class to instantiate
	 * @param argTypes     the constructor argument types, in order
	 * @param args         the constructor arguments
	 * @return a CodePiece
	 */
	public static CodePiece instantiate(String internalName, Type[] argTypes, CodePiece... args) {
		checkArgument(args.length == argTypes.length, "parameter list length mismatch");
		return concat(
				of(new TypeInsnNode(NEW, internalName)),
				ofOpcode(DUP),
				concat(args),
				of(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", getMethodDescriptor(VOID_TYPE, argTypes)))
		);
	}

	/**
	 * <p>Create a CodePiece that will cast the value on top of the stack to the given class.</p>
	 *
	 * @param c the class to cast to
	 * @return a CodePiece
	 */
	public static CodePiece castTo(Class<?> c) {
		return castTo(Type.getInternalName(c));
	}

	/**
	 * <p>Create a CodePiece that will cast the value on top of the stack to the given class.</p>
	 *
	 * @param type the type of the class to cast to
	 * @return a CodePiece
	 */
	public static CodePiece castTo(Type type) {
		return castTo(type.getInternalName());
	}

	/**
	 * <p>Create a CodePiece that will cast the value on top of the stack to the given class.</p>
	 *
	 * @param internalName the internal name of the class class to cast to
	 * @return a CodePiece
	 */
	public static CodePiece castTo(String internalName) {
		return of(new TypeInsnNode(CHECKCAST, internalName));
	}

	/**
	 * <p>Create a CodePiece that represents the given value, casted to the given class.</p>
	 *
	 * @param c     the class to cast to
	 * @param value a CodePiece representing the value
	 * @return a CodePiece
	 */
	public static CodePiece castTo(Class<?> c, CodePiece value) {
		return castTo(Type.getInternalName(c), value);
	}

	/**
	 * <p>Create a CodePiece that represents the given value, casted to the given class.</p>
	 *
	 * @param type  the type of the class to cast to
	 * @param value a CodePiece representing the value
	 * @return a CodePiece
	 */
	public static CodePiece castTo(Type type, CodePiece value) {
		return castTo(type.getInternalName(), value);
	}

	/**
	 * <p>Create a CodePiece that represents the given value, casted to the given class.</p>
	 *
	 * @param internalName the internal name of the class class to cast to
	 * @param value a CodePiece representing the value
	 * @return a CodePiece
	 */
	public static CodePiece castTo(String internalName, CodePiece value) {
		return value.append(new TypeInsnNode(CHECKCAST, internalName));
	}

	/**
	 * <p>Create a CodePieces that converts the primitive value to another type of primitive.</p>
	 * @param value the value
	 * @param from the original type of the value
	 * @param to the type to be converted to
	 * @return a CodePiece
	 */
	public static CodePiece castPrimitive(CodePiece value, Type from, Type to) {
		checkArgument(ASMUtils.isPrimitive(from) && ASMUtils.isPrimitive(to), "Types must be primivites");

		int from0 = from.getSort();
		int to0 = to.getSort();

		if (from0 == to0) {
			return value;
		}

		if (to0 == Type.BOOLEAN || from0 == Type.BOOLEAN) {
			throw new IllegalArgumentException("Cannot cast from " + from + " to " + to);
		}

		switch (to0) {
			case Type.BYTE:
				return toInt(value, from0).append(new InsnNode(I2B));
			case Type.SHORT:
				return toInt(value, from0).append(new InsnNode(I2S));
			case Type.CHAR:
				return toInt(value, from0).append(new InsnNode(I2C));
			case Type.INT:
				return toInt(value, from0);
			case Type.LONG:
				switch (from0) {
					case Type.FLOAT:
						return value.append(new InsnNode(F2L));
					case Type.DOUBLE:
						return value.append(new InsnNode(D2L));
					default:
						return value.append(new InsnNode(I2L));
				}
			case Type.FLOAT:
				switch (from0) {
					case Type.LONG:
						return value.append(new InsnNode(L2F));
					case Type.DOUBLE:
						return value.append(new InsnNode(D2F));
					default:
						return value.append(new InsnNode(I2F));
				}
			case Type.DOUBLE:
				switch (from0) {
					case Type.LONG:
						return value.append(new InsnNode(L2D));
					case Type.FLOAT:
						return value.append(new InsnNode(F2D));
					default:
						return value.append(new InsnNode(I2D));
				}
			default:
				throw new AssertionError();
		}
	}

	private static CodePiece toInt(CodePiece value, int fromType) {
		CodePiece intVal;
		switch (fromType) {
			case Type.LONG:
				intVal = value.append(new InsnNode(L2I));
				break;
			case Type.FLOAT:
				intVal = value.append(new InsnNode(F2I));
				break;
			case Type.DOUBLE:
				intVal = value.append(new InsnNode(D2I));
				break;
			default:
				intVal = value;
		}
		return intVal;
	}

	public static CodePiece doThrow(Class<? extends Exception> ex) {
		return doThrow(instantiate(ex));
	}

	public static CodePiece doThrow(Class<? extends Exception> ex, String msg) {
		return doThrow(instantiate(ex, new Type[] { getType(String.class) }, constant(msg)));
	}

	public static CodePiece doThrow(CodePiece ex) {
		return ex.append(new InsnNode(ATHROW));
	}

	/**
	 * <p>Create a CodePiece that unboxes the given primitive wrapper.</p>
	 * @param boxed the boxed value
	 * @param primitiveType the type of primitive boxed in the wrapper
	 * @return a CodePiece
	 */
	public static CodePiece unbox(CodePiece boxed, Class<?> primitiveType) {
		return unbox(boxed, getType(primitiveType));
	}

	/**
	 * <p>Create a CodePiece that unboxes the given primitive wrapper.</p>
	 * @param boxed the boxed value
	 * @param primitiveType the type of primitive boxed in the wrapper
	 * @return a CodePiece
	 */
	public static CodePiece unbox(CodePiece boxed, Type primitiveType) {
		Type boxedType = boxFor(primitiveType);
		String owner = boxedType.getInternalName();
		String name = primitiveType.getClassName() + "Value";
		String desc = Type.getMethodDescriptor(primitiveType);
		return invoke(INVOKEVIRTUAL, owner, name, desc, boxed);
	}

	/**
	 * <p>Create a CodePiece that boxes the given primitive into it's wrapper type.</p>
	 * @param unboxed the unboxed primitive
	 * @param primitiveType the type of primitive boxed in the wrapper
	 * @return a CodePiece
	 */
	public static CodePiece box(CodePiece unboxed, Class<?> primitiveType) {
		return box(unboxed, getType(primitiveType));
	}

	/**
	 * <p>Create a CodePiece that boxes the given primitive into it's wrapper type.</p>
	 * @param unboxed the unboxed primitive
	 * @param primitiveType the type of primitive boxed in the wrapper
	 * @return a CodePiece
	 */
	public static CodePiece box(CodePiece unboxed, Type primitiveType) {
		Type boxedType = boxFor(primitiveType);
		String owner = boxedType.getInternalName();
		String name = "valueOf";
		String desc = Type.getMethodDescriptor(boxedType, primitiveType);
		return invoke(INVOKESTATIC, owner, name, desc, unboxed);
	}

	private static Type boxFor(Type primitiveType) {
		switch (primitiveType.getSort()) {
			case BOOLEAN:
				return Type.getType(Boolean.class);
			case Type.BYTE:
				return Type.getType(Byte.class);
			case Type.SHORT:
				return Type.getType(Short.class);
			case Type.CHAR:
				return Type.getType(Character.class);
			case Type.INT:
				return Type.getType(Integer.class);
			case Type.LONG:
				return Type.getType(Long.class);
			case Type.FLOAT:
				return Type.getType(Float.class);
			case Type.DOUBLE:
				return Type.getType(Double.class);
			default:
				throw new IllegalArgumentException(primitiveType + "is not primitive");
		}
	}

	/**
	 * <p>Turn the given CodePiece into a lazily-initialized variable. That is, the value is not created until first accessed.
	 * After the first access, further accesses return the same, cached, value.</p>
	 * <p>This method cannot be used with primitive types or null.</p>
	 * @param var the variable to use for lazy-initialization
	 * @param valueCreator a CodePiece providing the value
	 * @return a CodePiece
	 */
	public static CodePiece makeLazy(ASMVariable var, CodePiece valueCreator) {
		checkArgument(!ASMUtils.isPrimitive(var.getType()), "cannot make primitive value lazy");
		CodeBuilder builder = new CodeBuilder();
		ContextKey context = ContextKey.create();
		LabelNode notNull = new LabelNode();

		if (var.isField() || var.isMethod()) {
			builder.add(var.get());
			builder.add(new InsnNode(DUP));
			builder.add(new JumpInsnNode(IFNONNULL, notNull), context);

			builder.add(new InsnNode(POP));
			builder.add(var.setAndGet(valueCreator));

			builder.add(notNull, context);
		} else {
			builder.add(var.get());
			builder.add(new JumpInsnNode(IFNONNULL, notNull), context);
			builder.add(var.set(valueCreator));

			builder.add(notNull, context);
			builder.add(var.get());
		}

		return builder.build();
	}

	private static CodePiece thisLoader;

	/**
	 * <p>Create a CodePiece that will load the current {@code this} reference onto the stack.</p>
	 *
	 * @return a CodePiece
	 */
	public static CodePiece getThis() {
		return thisLoader == null ? (thisLoader = of(new VarInsnNode(ALOAD, 0))) : thisLoader;
	}

	private static CodePiece nullLoader;

	/**
	 * <p>Create a CodePiece that will load {@code null} onto the stack.</p>
	 *
	 * @return a CodePiece
	 */
	public static CodePiece constantNull() {
		return nullLoader == null ? (nullLoader = of(new InsnNode(ACONST_NULL))) : nullLoader;
	}

	/**
	 * <p>Create a CodePiece that will load the given constant onto the stack.</p>
	 * <p>The object must be one of following:</p>
	 * <ul>
	 * <li>{@code null}</li>
	 * <li>A primitive wrapper ({@code Boolean}, {@code Byte}, {@code Short}, {@code Integer}, {@code Long}, {@code Character},
	 * {@code Float}, {@code Double}), will be loaded as the corresponding primitive</li>
	 * <li>A {@code String}</li>
	 * <li>An {@code Enum}</li>
	 * <li>A {@code Class}</li>
	 * <li>An ASM {@code Type}, will be loaded as the corresponding {@code Class} object</li>
	 * <li>A (possibly multi-dimensional) array with a component type of one of the above</li>
	 * </ul>
	 *
	 * @param o the constant
	 * @return a CodePiece
	 */
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
		} else if (o instanceof Character) {
			return constant((char) o);
		} else if (o instanceof Long) {
			return constant((long) o);
		} else if (o instanceof Float) {
			return constant((float) o);
		} else if (o instanceof Double) {
			return constant((double) o);
		} else if (o instanceof String || o instanceof Type) {
			return ldcConstant(o);
		} else if (o instanceof Enum) {
			return constant((Enum<?>) o);
		} else if (o instanceof Class) {
			return constant(((Class<?>) o));
		} else if (o.getClass().isArray()) {
			return arrayConstant(o);
		}
		throw new IllegalArgumentException("Invalid constant: " + o);
	}

	/**
	 * <p>Create a CodePiece that will load the given constant onto the stack.</p>
	 *
	 * @param b the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(boolean b) {
		return constant(b ? 1 : 0);
	}

	/**
	 * <p>Create a CodePiece that will load the given constant onto the stack.</p>
	 *
	 * @param i the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(int i) {
		return of(intConstant(i));
	}

	/**
	 * <p>Create a CodePiece that will load the given constant onto the stack.</p>
	 *
	 * @param l the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(long l) {
		if (l == 0) {
			return ofOpcode(LCONST_0);
		} else if (l == 1) {
			return ofOpcode(LCONST_1);
		} else {
			return ldcConstant(l);
		}
	}

	/**
	 * <p>Create a CodePiece that will load the given constant onto the stack.</p>
	 *
	 * @param f the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(float f) {
		if (f == 0f) {
			return ofOpcode(FCONST_0);
		} else if (f == 1f) {
			return ofOpcode(FCONST_1);
		} else if (f == 2f) {
			return ofOpcode(FCONST_2);
		} else {
			return ldcConstant(f);
		}
	}

	/**
	 * <p>Create a CodePiece that will load the given constant onto the stack.</p>
	 *
	 * @param d the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(double d) {
		if (d == 0d) {
			return ofOpcode(DCONST_0);
		} else if (d == 1d) {
			return ofOpcode(DCONST_1);
		} else {
			return ldcConstant(d);
		}
	}

	/**
	 * <p>Create a CodePiece that will load the given constant onto the stack.</p>
	 *
	 * @param s the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(String s) {
		return ldcConstant(s);
	}

	/**
	 * <p>Create a CodePiece that will load the given class constant onto the stack.</p>
	 *
	 * @param t the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(Type t) {
		return ldcConstant(t);
	}

	/**
	 * <p>Create a CodePiece that will load the given class constant onto the stack.</p>
	 *
	 * @param c the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(Class<?> c) {
		return c == null ? constantNull() : ldcConstant(Type.getType(c));
	}

	/**
	 * <p>Create a CodePiece that will load the given enum constant onto the stack.</p>
	 *
	 * @param e the constant
	 * @return a CodePiece
	 */
	public static CodePiece constant(Enum<?> e) {
		if (e == null) {
			return constantNull();
		}
		Class<? extends Enum<?>> enumClass = e.getDeclaringClass();
		return getField(Type.getInternalName(enumClass), e.name(), enumClass);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(boolean[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(byte[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(short[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(int[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(long[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(char[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(float[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(double[] arr) {
		return arrayConstant(arr);
	}

	/**
	 * <p>Create a CodePiece that will load the given array onto the stack.</p>
	 * <p>The array may only contain valid constants (see {@link #constant(Object)}</p>
	 *
	 * @param arr the array
	 * @return a CodePiece
	 */
	public static CodePiece constant(Object[] arr) {
		return arrayConstant(arr);
	}

	private static CodePiece arrayConstant(Object arr) {
		Type compType = findCorrespondingType(Type.getType(arr.getClass().getComponentType()));

		int len = Array.getLength(arr);

		CodeBuilder builder = new CodeBuilder();
		builder.add(intConstant(len));

		if (ASMUtils.isPrimitive(compType)) {
			builder.add(new IntInsnNode(NEWARRAY, toArrayType(compType)));
		} else {
			builder.add(new TypeInsnNode(ANEWARRAY, compType.getInternalName()));
		}

		int storeOpcode = compType.getOpcode(IASTORE);

		for (int i = 0; i < len; ++i) {
			builder.add(new InsnNode(DUP))
					.add(intConstant(i))
					.add(constant(Array.get(arr, i)))
					.add(new InsnNode(storeOpcode));
		}

		return builder.build();
	}

	private static Type findCorrespondingType(Type type) {
		String internalName = type.getInternalName();
		if (internalName.equals(Type.getInternalName(Type.class))) {
			return Type.getType(Class.class);
		}
		return type;
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

	private static AbstractInsnNode intConstant(int i) {
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
				if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
					return new IntInsnNode(BIPUSH, i);
				} else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
					return new IntInsnNode(SIPUSH, i);
				} else {
					return new LdcInsnNode(i);
				}
		}
	}

	private static CodePiece ldcConstant(Object o) {
		if (o == null) {
			return constantNull();
		} else {
			return of(new LdcInsnNode(o));
		}
	}

}
