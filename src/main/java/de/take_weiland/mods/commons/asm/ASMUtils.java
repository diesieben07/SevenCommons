package de.take_weiland.mods.commons.asm;

import com.google.common.base.*;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.Reflection;
import de.take_weiland.mods.commons.OverrideSetter;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * <p>A collection of utility methods for working with the ASM library.</p>
 */
@ParametersAreNonnullByDefault
public final class ASMUtils {

	private ASMUtils() { }

	/**
	 * <p>The {@code Object} class as an ASM Type.</p>
	 */
	public static final Type OBJECT_TYPE = Type.getType(Object.class);

	// *** bytecode analyzing helpers *** //

	/**
	 * <p>Find the last return instruction in the given method.</p>
	 *
	 * @param method the method
	 * @return the last return instruction
	 * @throws java.lang.IllegalArgumentException if the method doesn't have valid return opcode (should never happen with any valid method)
	 */
	public static AbstractInsnNode findLastReturn(MethodNode method) {
		AbstractInsnNode node = findLast(method.instructions, Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN));
		if (node == null) {
			throw new IllegalArgumentException("Illegal method: Has no or wrong return opcode!");
		}
		return node;
	}

	/**
	 * <p>Find the last instruction with the given opcode in the given InsnList.</p>
	 *
	 * @param insns  the InsnList
	 * @param opcode the opcode to find
	 * @return the last instruction with the given opcode
	 */
	public static AbstractInsnNode findLast(InsnList insns, int opcode) {
		AbstractInsnNode node = insns.getLast();
		while (node != null) {
			if (node.getOpcode() == opcode) {
				return node;
			}
			node = node.getPrevious();
		}
		return null;
	}

	/**
	 * <p>Find the first instruction with the given opcode in the given InsnList.</p>
	 *
	 * @param insns  the InsnList
	 * @param opcode the opcode to find
	 * @return the first instruction with the given opcode
	 */
	public static AbstractInsnNode findFirst(InsnList insns, int opcode) {
		AbstractInsnNode node = insns.getFirst();
		while (node != null) {
			if (node.getOpcode() == opcode) {
				return node;
			}
			node = node.getNext();
		}
		return null;
	}

	// *** member finding helpers *** //

	/**
	 * <p>Find the field with the given name.</p>
	 *
	 * @param clazz the class
	 * @param name  the field name to search for
	 * @return the field with the given name or null if no such field was found
	 */
	public static FieldNode findField(ClassNode clazz, String name) {
		for (FieldNode field : clazz.fields) {
			if (field.name.equals(name)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * <p>Find the field with the given name. It is automatically chosen between MCP name and SRG name, depending on the runtime environment.</p>
	 *
	 * @param clazz the class
	 * @param srg   the SRG name of the field (e.g. field_70123_h)
	 * @return the field with the given name or null if no such field was found
	 */
	public static FieldNode findMinecraftField(ClassNode clazz, String srg) {
		return findField(clazz, MCPNames.field(srg));
	}

	/**
	 * <p>Find the field with the given name.</p>
	 *
	 * @param clazz the class
	 * @param name  the field name to search for
	 * @return the field with the given name
	 * @throws de.take_weiland.mods.commons.asm.MissingFieldException if no such field was found
	 */
	public static FieldNode requireField(ClassNode clazz, String name) {
		FieldNode field = findField(clazz, name);
		if (field == null) {
			throw new MissingFieldException(clazz.name, name);
		}
		return field;
	}

	/**
	 * <p>Find the field with the given name. The name will be automatically translated to the MCP name if needed, depending on the runtime environment.</p>
	 *
	 * @param clazz the class
	 * @param srg   the SRG name of the field (e.g. field_70123_h)
	 * @return the field with the given name
	 * @throws de.take_weiland.mods.commons.asm.MissingFieldException if no such field was found
	 */
	public static FieldNode requireMinecraftField(ClassNode clazz, String srg) {
		return requireField(clazz, MCPNames.field(srg));
	}

	/**
	 * <p>Find the method with the given name. If multiple methods with the same name exist, the first one will be returned.</p>
	 *
	 * @param clazz the class
	 * @param name  the method name to search for
	 * @return the first method with the given name or null if no such method is found
	 */
	public static MethodNode findMethod(ClassNode clazz, String name) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * <p>Find the method with the given name and method descriptor.</p>
	 *
	 * @param clazz the class
	 * @param name  the method name to search for
	 * @param desc  the method descriptor to search for
	 * @return the method with the given name and descriptor or null if no such method is found
	 */
	public static MethodNode findMethod(ClassNode clazz, String name, String desc) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name) && method.desc.equals(desc)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * <p>Find the method with the given name. It is automatically chosen between MCP and SRG name, depending on the runtime environment.</p>
	 *
	 * @param clazz   the class
	 * @param srgName the SRG name of the method (e.g. {@code func_70316_g}
	 * @return the method matching the given SRG name or null if no such method is found
	 */
	public static MethodNode findMinecraftMethod(ClassNode clazz, String srgName) {
		return findMethod(clazz, MCPNames.method(srgName));
	}

	/**
	 * <p>Find the method with the given name. If multiple methods with the same name exist, the first one will be returned.</p>
	 *
	 * @param clazz the class
	 * @param name  the method name to search for
	 * @return the first method with the given name
	 * @throws de.take_weiland.mods.commons.asm.MissingMethodException if no such method was found
	 */
	public static MethodNode requireMethod(ClassNode clazz, String name) {
		MethodNode m = findMethod(clazz, name);
		if (m == null) {
			throw MissingMethodException.create(name, clazz.name);
		}
		return m;
	}

	/**
	 * <p>Find the method with the given name and method descriptor.</p>
	 *
	 * @param clazz the class
	 * @param name  the method name to search for
	 * @param desc  the method descriptor to search for
	 * @return the method with the given name and descriptor
	 * @throws de.take_weiland.mods.commons.asm.MissingMethodException if no such method was found
	 */
	public static MethodNode requireMethod(ClassNode clazz, String name, String desc) {
		MethodNode m = findMethod(clazz, name, desc);
		if (m == null) {
			throw MissingMethodException.withDesc(name, desc, clazz.name);
		}
		return m;
	}

	/**
	 * <p>Find the method with the given name. It is automatically chosen between MCP and SRG name, depending on the runtime environment.</p>
	 *
	 * @param clazz   the class
	 * @param srgName the SRG name of the method (e.g. {@code func_70316_g}
	 * @return the method matching the given SRG name
	 * @throws de.take_weiland.mods.commons.asm.MissingMethodException if no such method was found
	 */
	public static MethodNode requireMinecraftMethod(ClassNode clazz, String srgName) {
		MethodNode m = findMinecraftMethod(clazz, srgName);
		if (m == null) {
			throw MissingMethodException.create(srgName, clazz.name);
		}
		return m;
	}

	/**
	 * <p>Add the given code to the instance-initializer of the given class.</p>
	 * <p>Effectively this class inserts the code into every root-constructor of the class. If no constructor is present,
	 * this method will create the default constructor, otherwise the code will be prepended to every constructor.</p>
	 * <p>The code may not contain exitpoints (such as return or throws) or this method may produce faulty code.</p>
	 *
	 * @param clazz the class
	 * @param code  the code to initialize
	 */
	public static void initialize(ClassNode clazz, CodePiece code) {
		List<MethodNode> rootCtsrs = getRootConstructors(clazz);
		if (rootCtsrs.isEmpty()) {
			String name = "<init>";
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE);

			MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
			method.instructions.add(new VarInsnNode(ALOAD, 0));
			method.instructions.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
			method.instructions.add(new InsnNode(RETURN));
			clazz.methods.add(method);
			rootCtsrs = Collections.singletonList(method);
		}
		for (MethodNode cstr : rootCtsrs) {
			code.insertAfter(cstr.instructions, findFirst(cstr.instructions, INVOKESPECIAL));
		}
	}

	/**
	 * <p>Add the given code to the static-initializer of the given class.</p>
	 * <p>If the static-initializer is not present, this method will create it, otherwise the code will be prepended to
	 * the already present instructions.</p>
	 * <p>The code may not contain exitpoints (such as return or throws) or this method may produce faulty code.</p>
	 *
	 * @param clazz the class
	 * @param code  the code to initialize
	 */
	public static void initializeStatic(ClassNode clazz, CodePiece code) {
		MethodNode method = findMethod(clazz, "<clinit>");
		if (method == null) {
			method = new MethodNode(ACC_PUBLIC | ACC_STATIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
			method.instructions.add(new InsnNode(RETURN));
			clazz.methods.add(method);
		}
		code.prependTo(method.instructions);
	}

	private static final String MUTEX_NAME = "_sc$lateinit$m";
	private static final String INIT_METHOD = "_sc$lateinit";
	private static final String INIT_CODE_METHOD = "_sc$lateinit$c";
	private static final String HAS_INIT_FIELD = "_sc$lateinit$f";

	/**
	 * <p>Add the given code to the late-static initializer of the given class. The code will be executed the first time any
	 * constructor is called, immediately after the super constructor is called.</p>
	 * <p>The generated code will be threadsafe and have almost 0 overhead on constructor invocation.</p>
	 * <p>The code may not contain exitpoints (such as return or throws) or this method may produce faulty code.</p>
	 * @param clazz the class
	 * @param code the code to execute
	 */
	public static void initializeLate(ClassNode clazz, CodePiece code) {
		FieldNode mutexField = findField(clazz, MUTEX_NAME);
		MethodNode codeMethod;
		if (mutexField == null) {
			mutexField = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, MUTEX_NAME, Type.getDescriptor(Object.class), null, null);
			clazz.fields.add(mutexField);
			initializeStatic(clazz, CodePieces.setField(clazz, mutexField, CodePieces.instantiate(Object.class)));

			FieldNode hasInitField = new FieldNode(ACC_PRIVATE | ACC_STATIC, HAS_INIT_FIELD, Type.BOOLEAN_TYPE.getDescriptor(), null, null);
			clazz.fields.add(hasInitField);

			codeMethod = new MethodNode(ACC_PRIVATE, INIT_CODE_METHOD, Type.getMethodDescriptor(VOID_TYPE), null, null);
			clazz.methods.add(codeMethod);
			codeMethod.instructions.add(new InsnNode(RETURN));

			MethodNode initMethod = new MethodNode(ACC_PRIVATE, INIT_METHOD, Type.getMethodDescriptor(VOID_TYPE), null, null);
			clazz.methods.add(initMethod);
			InsnList insns = initMethod.instructions;

			ASMCondition hasInit = ASMCondition.isTrue(CodePieces.getField(clazz, hasInitField));

			LabelNode start = new LabelNode();
			LabelNode end = new LabelNode();
			LabelNode finallyLbl = new LabelNode();
			LabelNode finally2nd = new LabelNode();

			CodePieces.getField(clazz, mutexField).appendTo(insns);
			insns.add(new InsnNode(DUP));
			insns.add(new VarInsnNode(ASTORE, 1));
			insns.add(new InsnNode(MONITORENTER));
			insns.add(start);

			hasInit.doIfTrue(CodePieces.of(new InsnNode(RETURN)))
					.appendTo(insns);

			CodePieces.invokeSpecial(clazz.name, INIT_CODE_METHOD, CodePieces.getThis(), void.class)
					.appendTo(insns);

			CodePieces.setField(clazz, hasInitField, CodePieces.constant(true))
					.appendTo(insns);

			insns.add(new VarInsnNode(ALOAD, 1));
			insns.add(new InsnNode(MONITOREXIT));
			insns.add(end);

			insns.add(new InsnNode(RETURN));

			insns.add(finallyLbl);
			insns.add(new VarInsnNode(ASTORE, 2));
			insns.add(new VarInsnNode(ALOAD, 1));
			insns.add(new InsnNode(MONITOREXIT));

			insns.add(finally2nd);

			insns.add(new VarInsnNode(ALOAD, 2));
			insns.add(new InsnNode(ATHROW));

			initMethod.tryCatchBlocks.add(new TryCatchBlockNode(start, end, finallyLbl, null));
			initMethod.tryCatchBlocks.add(new TryCatchBlockNode(finallyLbl, finally2nd, finallyLbl, null));

			CodePiece invoke = CodePieces.invokeVirtual(clazz.name, INIT_METHOD, CodePieces.getThis(), void.class);
			initialize(clazz, hasInit.doIfFalse(invoke));
		} else {
			codeMethod = requireMethod(clazz, INIT_CODE_METHOD);
		}

		code.prependTo(codeMethod.instructions);
	}

	/**
	 * <p>Determine if the given method is a constructor.</p>
	 *
	 * @param method the method
	 * @return true if the method is a constructor
	 */
	public static boolean isConstructor(MethodNode method) {
		return method.name.equals("<init>");
	}

	static Predicate<MethodNode> predIsConstructor() {
		return new Predicate<MethodNode>() {
			@Override
			public boolean apply(@Nullable MethodNode input) {
				return isConstructor(checkNotNull(input));
			}
		};
	}

	/**
	 * <p>Get all constructors of the given class.</p>
	 * <p>The returned collection is a live-view, so if new constructors get added, they will be present in the returned collection immediately.</p>
	 *
	 * @param clazz the class
	 * @return all constructors
	 */
	public static Collection<MethodNode> getConstructors(ClassNode clazz) {
		return Collections2.filter(clazz.methods, predIsConstructor());
	}

	/**
	 * <p>Determine if the method in the given {@code targetClass} can be accessed from the {@code accessingClass}.</p>
	 *
	 * @param accessingClass the class trying to access the field
	 * @param targetClass    the class containing the field
	 * @param method         the method to check for
	 * @return if the given method can be accessed
	 */
	public static boolean isAccessibleFrom(ClassNode accessingClass, ClassNode targetClass, MethodNode method) {
		return isAccessibleFrom(accessingClass, targetClass, method.access);
	}

	/**
	 * <p>Determine if the field in the given {@code targetClass} can be accessed from the {@code accessingClass}.</p>
	 *
	 * @param accessingClass the class trying to access the field
	 * @param targetClass    the class containing the field
	 * @param field          the field to check for
	 * @return if the given field can be accessed
	 */
	public static boolean isAccessibleFrom(ClassNode accessingClass, ClassNode targetClass, FieldNode field) {
		return isAccessibleFrom(accessingClass, targetClass, field.access);
	}

	/**
	 * <p>Determine if a member with the given access-modifiers in {@code targetClass} can be accessed from
	 * {@code accessingClass}.</p>
	 * @param accessingClass the accessing class
	 * @param targetClass the target class
	 * @param targetAccess the access-modifiers of the target member
	 * @return true if the member is accessible
	 */
	public static boolean isAccessibleFrom(ClassNode accessingClass, ClassNode targetClass, int targetAccess) {
		return isAccessibleFrom(ClassInfo.of(accessingClass), ClassInfo.of(targetClass), targetAccess);
	}

	/**
	 * <p>Determine if a member with the given access-modifiers in {@code targetClass} can be accessed from
	 * {@code accessingClass}.</p>
	 * @param accessingClass the accessing class
	 * @param targetClass the target class
	 * @param targetAccess the access-modifiers of the target member
	 * @return true if the member is accessible
	 */
	public static boolean isAccessibleFrom(ClassInfo accessingClass, ClassInfo targetClass, int targetAccess) {
		// public methods are reachable from everywhere
		if ((targetAccess & ACC_PUBLIC) == ACC_PUBLIC) {
			return true;
		}

		// classes can access their own members
		if (accessingClass.internalName().equals(targetClass.internalName())) {
			return true;
		}

		// private members are only reachable from within the same class
		if ((targetAccess & ACC_PRIVATE) == ACC_PRIVATE) {
			return false;
		}

		// member can only be protected or package-private at this point
		// if package is equal, it's accessible
		if (getPackage(accessingClass.internalName()).equals(getPackage(targetClass.internalName()))) {
			return true;
		}

		// if member is protected, check the class hierarchy
		return (targetAccess & ACC_PROTECTED) == ACC_PROTECTED && targetClass.isAssignableFrom(targetClass);
	}

	private static String getPackage(String internalName) {
		int idx = internalName.lastIndexOf('/');
		return idx == -1 ? "" : internalName.substring(0, idx);
	}

	/**
	 * <p>Get all root constructors of the given class. A root constructor that does not delegate to another constructor of the same class.</p>y
	 *
	 * @param clazz the class
	 * @return all root constructors
	 */
	public static List<MethodNode> getRootConstructors(ClassNode clazz) {
		ImmutableList.Builder<MethodNode> builder = ImmutableList.builder();

		cstrs:
		for (MethodNode method : getConstructors(clazz)) {
			AbstractInsnNode insn = method.instructions.getFirst();
			do {
				if (insn.getOpcode() == INVOKESPECIAL && ((MethodInsnNode) insn).owner.equals(clazz.name)) {
					continue cstrs;
				}
				insn = insn.getNext();
			} while (insn != null);
			builder.add(method);
		}
		return builder.build();
	}

	/**
	 * <p>Get all methods in the given class which have the given annotation.</p>
	 *
	 * @param clazz      the ClassNode
	 * @param annotation the annotation to search for
	 * @return a Collection containing all methods in the class which have the given annotation
	 */
	public static Collection<MethodNode> methodsWith(ClassNode clazz, final Class<? extends Annotation> annotation) {
		checkNotNull(annotation, "annotation");
		return Collections2.filter(clazz.methods, new Predicate<MethodNode>() {
			@Override
			public boolean apply(@Nullable MethodNode method) {
				return hasAnnotation(checkNotNull(method), annotation);
			}
		});
	}

	/**
	 * <p>Get all fields in the given class which have the given annotation.</p>
	 *
	 * @param clazz      the ClassNode
	 * @param annotation the annotation to search for
	 * @return a Collection containing all fields in the class which have the given annotation
	 */
	public static Collection<FieldNode> fieldsWith(ClassNode clazz, final Class<? extends Annotation> annotation) {
		return Collections2.filter(clazz.fields, new Predicate<FieldNode>() {
			@Override
			public boolean apply(@Nullable FieldNode field) {
				return hasAnnotation(checkNotNull(field), annotation);
			}
		});
	}

	private static Type getterType(MethodNode getter) {
		Type returnType = Type.getReturnType(getter.desc);
		if (returnType == Type.VOID_TYPE || Type.getArgumentTypes(getter.desc).length != 0) {
			throw new IllegalArgumentException("Invalid Getter!");
		}
		return returnType;
	}

	/**
	 * <p>Find the setter for a given getter. This method gives precedence to the {@link de.take_weiland.mods.commons.OverrideSetter} annotation.
	 * If it is not present, the following rules apply:</p>
	 * <ul>
	 * <li>getFoobar => setFoobar</li>
	 * <li>isFoobar => setFoobar</li>
	 * <li>foobar => foobar (in .java files)</li>
	 * <li>foobar => foobar_$eq (in .scala files)</li>
	 * </ul>
	 *
	 * @param clazz  the class containing the getter
	 * @param getter the getter method
	 * @return the setter corresponding to the given getter, or null if no setter was found
	 */
	public static MethodNode findSetter(ClassNode clazz, MethodNode getter) {
		Type type = getterType(getter);

		String setterName;

		AnnotationNode overrideSetter = getRawAnnotation(getter, OverrideSetter.class);
		if (overrideSetter != null) {
			setterName = getAnnotationProperty(overrideSetter, "value", OverrideSetter.class);
		} else {
			if (getter.name.startsWith("get") && getter.name.length() >= 4 && Character.isUpperCase(getter.name.charAt(3))) {
				setterName = "set" + getter.name.substring(3);
			} else if (getter.name.startsWith("is") && getter.name.length() >= 3 && Character.isUpperCase(getter.name.charAt(2))) {
				setterName = "set" + getter.name.substring(2);
			} else if (isScala(clazz)) {
				setterName = getter.name + "_$eq";
			} else {
				setterName = getter.name;
			}
		}
		String setterDesc = Type.getMethodDescriptor(Type.VOID_TYPE, type);
		return findMethod(clazz, setterName, setterDesc);
	}

	/**
	 * <p>Walks {@code n} steps forwards in the InsnList of the given instruction.</p>
	 *
	 * @param insn the starting point
	 * @param n    how many steps to move forwards
	 * @return the instruction {@code n} steps forwards
	 * @throws java.lang.IndexOutOfBoundsException if the list ends before n steps have been walked
	 */
	public static AbstractInsnNode getNext(AbstractInsnNode insn, int n) {
		@Nullable AbstractInsnNode curr = insn;
		for (int i = 0; i < n; ++i) {
			curr = curr.getNext();
			if (curr == null) {
				throw new IndexOutOfBoundsException();
			}
		}
		return curr;
	}

	/**
	 * <p>Walks {@code n} steps backwards in the InsnList of the given instruction.</p>
	 *
	 * @param insn the starting point
	 * @param n    how many steps to move backwards
	 * @return the instruction {@code n} steps backwards
	 * @throws java.lang.IndexOutOfBoundsException if the list ends before n steps have been walked
	 */
	public static AbstractInsnNode getPrevious(AbstractInsnNode insn, int n) {
		@Nullable AbstractInsnNode curr = insn;
		for (int i = 0; i < n; ++i) {
			curr = curr.getPrevious();
			if (curr == null) {
				throw new IndexOutOfBoundsException();
			}
		}
		return insn;
	}

	/**
	 * <p>Creates a clone of the given InsnList.</p>
	 * @param insns the InsnList
	 * @return the cloned list
	 */
	public static InsnList clone(InsnList insns) {
		return clone(insns, labelCloneMap(insns.getFirst()));
	}

	/**
	 * <p>Create a clone of the given InsnList using the given Label remaps.</p>
	 * @param list the InsnList
	 * @param map a Map that maps all labels in the list to the new labels
	 * @return the cloned list
	 */
	public static InsnList clone(InsnList list, Map<LabelNode, LabelNode> map) {
		InsnList cloned = new InsnList();
		AbstractInsnNode current = list.getFirst();
		while (current != null) {
			cloned.add(current.clone(map));
			current = current.getNext();
		}
		return cloned;
	}

	private static Map<LabelNode, LabelNode> labelCloneMap(final AbstractInsnNode first) {
		ImmutableMap.Builder<LabelNode, LabelNode> b = ImmutableMap.builder();
		AbstractInsnNode current = first;
		do {
			if (current instanceof LabelNode) {
				b.put((LabelNode) current, new LabelNode());
			}
			current = current.getNext();
		} while (current != null);
		return b.build();
	}

	// *** name utilities *** //

	/**
	 * <p>Get the descriptor for a method with the given return type and the given arguments.</p>
	 * @param returnType the return type of the method
	 * @param args the arguments of the method
	 * @return the descriptor
	 */
	public static String getMethodDescriptor(Class<?> returnType, Class<?>... args) {
		StringBuilder b = new StringBuilder();
		b.append('(');
		for (Class<?> arg : args) {
			b.append(Type.getDescriptor(arg));
		}
		b.append(')');
		b.append(Type.getDescriptor(returnType));

		return b.toString();
	}

	/**
	 * <p>Convert the given binary name (e.g. {@code java.lang.Object$Subclass}) to an internal name (e.g. {@code java/lang/Object$Subclass}).</p>
	 *
	 * @param binaryName the binary name
	 * @return the internal name
	 */
	public static String internalName(String binaryName) {
		return binaryName.replace('.', '/');
	}

	/**
	 * <p>Convert the given internal name to a binary name (opposite of {@link #internalName(String)}).</p>
	 *
	 * @param internalName the internal name
	 * @return the binary name
	 */
	public static String binaryName(String internalName) {
		return internalName.replace('/', '.');
	}

	/**
	 * <p>Get the descriptor for the given internal name.</p>
	 * @param internalName the internal name
	 * @return the descriptor
	 *
	 * @see org.objectweb.asm.Type#getDescriptor()
	 */
	public static String getDescriptor(String internalName) {
		switch (internalName) {
			case "boolean":
				return "Z";
			case "byte":
				return "B";
			case "short":
				return "S";
			case "char":
				return "C";
			case "int":
				return "I";
			case "long":
				return "L";
			case "float":
				return "F";
			case "double":
				return "D";
			default:
				return 'L' + internalName + ';';
		}
	}

	private static IClassNameTransformer nameTransformer;
	private static boolean nameTransChecked = false;

	/**
	 * <p>Get the active {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any.</p>
	 *
	 * @return the active transformer, or null if none
	 */
	@Nullable
	public static IClassNameTransformer getClassNameTransformer() {
		if (!nameTransChecked) {
			nameTransformer = FluentIterable.from(Launch.classLoader.getTransformers())
					.filter(IClassNameTransformer.class)
					.first().orNull();
			nameTransChecked = true;
		}
		return nameTransformer;
	}

	/**
	 * <p>Transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any. Returns the untransformed
	 * name if no transformer is present.</p>
	 *
	 * @param untransformedName the un-transformed internal name of the class
	 * @return the transformed internal name of the class
	 */
	public static String transformName(String untransformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? untransformedName : t.remapClassName(binaryName(untransformedName)));
	}

	/**
	 * <p>Un-transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any. Returns
	 * the transformed name if no transformer is present.</p>
	 *
	 * @param transformedName the transformed internal name of the class
	 * @return the un-transformed internal name of the class
	 */
	public static String untransformName(String transformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? transformedName : t.unmapClassName(binaryName(transformedName)));
	}

	// *** Misc Utils *** //

	/**
	 * <p>Create a ClassNode for the class specified by the given internal name. The class must be accessible
	 * by the {@link net.minecraft.launchwrapper.Launch#classLoader LaunchClassLoader}.</p>
	 * @param name the internal name
	 * @return a ClassNode
	 * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
	 */
	public static ClassNode getClassNode(String name) {
		return getClassNode(name, 0);
	}

	/**
	 * <p>Create a ClassNode for the class specified by the given internal name. The class must be accessible
	 * by the {@link net.minecraft.launchwrapper.Launch#classLoader LaunchClassLoader}.</p>
	 *
	 * @param name the internal name
	 * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
	 * @return a ClassNode
	 * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
	 */
	public static ClassNode getClassNode(String name, int readerFlags) {
		try {
			byte[] bytes = Launch.classLoader.getClassBytes(untransformName(name));
			if (bytes == null) {
				throw new MissingClassException(name);
			}
			return getClassNode(bytes, readerFlags);
		} catch (Exception e) {
			Throwables.propagateIfInstanceOf(e, MissingClassException.class);
			throw new MissingClassException(name, e);
		}
	}

	/**
	 * <p>Create a ClassNode for the class represented by the given bytes.</p>
	 * @param bytes the class bytes
	 * @return a ClassNode
	 */
	public static ClassNode getClassNode(byte[] bytes) {
		return getClassNode(bytes, 0);
	}

	/**
	 * <p>Create a ClassNode for the class represented by the given bytes.</p>
	 *
	 * @param bytes the class bytes
	 * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
	 * @return a ClassNode
	 */
	public static ClassNode getClassNode(byte[] bytes, int readerFlags) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode clazz = new ClassNode();
		reader.accept(clazz, readerFlags);
		return clazz;
	}

	private static final int THIN_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

	/**
	 * <p>Create a ClassNode for the class specified by the given internal name. The class must be accessible
	 * by the {@link net.minecraft.launchwrapper.Launch#classLoader LaunchClassLoader}.</p>
	 * <p>The ClassNode will only contain the rough outline of the class. It is read with {@code ClassReader.SKIP_CODE}, {@code ClassReader.SKIP_DEBUG}
	 * {@code ClassReader.SKIP_FRAMES} set.</p>
	 * @param name the internal name
	 * @return a ClassNode
	 *
	 */
	public static ClassNode getThinClassNode(String name) {
		return getClassNode(name, THIN_FLAGS);
	}

	/**
	 * <p>Create a ClassNode for the class represented by the given bytes.</p>
	 * <p>The ClassNode will only contain the rough outline of the class. It is read with {@code ClassReader.SKIP_CODE}, {@code ClassReader.SKIP_DEBUG}
	 * {@code ClassReader.SKIP_FRAMES} set.</p>
	 * @param bytes the class bytes
	 * @return a ClassNode
	 *
	 */
	public static ClassNode getThinClassNode(byte[] bytes) {
		return getClassNode(bytes, THIN_FLAGS);
	}

	/**
	 * <p>Tries to determine if the given ClassNode represents a scala class.</p>
	 *
	 * @param clazz the ClassNode
	 * @return true if the class is a scala class, false if it is most likely a non-scala class
	 */
	public static boolean isScala(ClassNode clazz) {
		return Strings.nullToEmpty(clazz.sourceFile).toLowerCase().endsWith(".scala")
				|| hasAnnotation(clazz, "scala/reflect/ScalaSignature")
				|| hasAnnotation(clazz, "scala/reflect/ScalaLongSignature");
	}

	// *** annotation utilities *** //

	/**
	 * <p>Turn the given AnnotationNode into an actual instance of it's annotation class.</p>
	 * <p>If {@code annotationNode} is null, null is returned.</p>
	 * @param annotationNode the AnnotationNode
	 * @param annotationClass the annotation class
	 * @return a instance of the annotation or null
	 */
	public static <A extends Annotation> A makeReal(@Nullable AnnotationNode annotationNode, final Class<A> annotationClass) {
		if (annotationNode == null) {
			return null;
		}
		if (!annotationNode.desc.equals(Type.getDescriptor(annotationClass))) {
			throw new IllegalArgumentException("Annotation mismatch");
		}

		final Map<String, Object> map = buildMap(annotationNode, annotationClass);

		// This proxy class is created most likely anyways
		// since Annotations use them internally, too
		return Reflection.newProxy(annotationClass, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
				String name = method.getName();
				if (name.equals("equals") && args != null && args.length == 1) {
					return annEqImpl(map, annotationClass, args[0]);
				}
				if (args != null) {
					throw new NoSuchMethodError();
				}
				switch (name) {
					case "hashCode":
						return annHashCodeImpl(annotationClass, map);
					case "toString":
						return annToStringImpl(annotationClass, map);
					case "annotationType":
						return annotationClass;
					default:
						Object val = map.get(name);
						if (val == null) {
							return method.getDefaultValue();
						}
						return val;
				}
			}
		});
	}

	/**
	 * <p>Get the annotation of the given class, if present on the given ClassNode, null otherwise.</p>
	 * @param clazz the ClassNode
	 * @param annotation the annotation class
	 * @return the annotation or null
	 */
	public static <A extends Annotation> A getAnnotation(ClassNode clazz, Class<A> annotation) {
		return makeReal(getRawAnnotation(clazz, annotation), annotation);
	}

	/**
	 * <p>Get the annotation of the given class, if present on the given FieldNode, null otherwise.</p>
	 * @param field the FieldNode
	 * @param annotation the annotation class
	 * @return the annotation or null
	 */
	public static <A extends Annotation> A getAnnotation(FieldNode field, Class<A> annotation) {
		return makeReal(getRawAnnotation(field, annotation), annotation);
	}

	/**
	 * <p>Get the annotation of the given class, if present on the given MethodNode, null otherwise.</p>
	 * @param method the MethodNode
	 * @param annotation the annotation class
	 * @return the annotation or null
	 */
	public static <A extends Annotation> A getAnnotation(MethodNode method, Class<A> annotation) {
		return makeReal(getRawAnnotation(method, annotation), annotation);
	}

	private static Map<String, Object> buildMap(AnnotationNode ann, Class<? extends Annotation> clazz) {
		ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

		int len = ann.values == null ? 0 : ann.values.size();
		try {
			for (int i = 0; i < len; i += 2) {
				String key = (String) ann.values.get(i);
				Object val = ann.values.get(i + 1);
				Method method = clazz.getDeclaredMethod(key, ArrayUtils.EMPTY_CLASS_ARRAY);

				builder.put(key, decodeAnnotationVal(val, method.getReturnType()));
			}
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			throw JavaUtils.throwUnchecked(e);
		}

		return builder.build();
	}

	private static Object decodeAnnotationVal(Object val, Class<?> type) throws ClassNotFoundException {
		Class<?> cls = val.getClass();
		if (Primitives.isWrapperType(cls) || cls == String.class) {
			return val;
		} else if (cls == Type.class) {
			Type t = (Type) val;
			switch (t.getSort()) {
				case Type.OBJECT:
					return Class.forName(t.getClassName());
				case Type.ARRAY:
					return Class.forName(binaryName(t.getInternalName()));
				case Type.BOOLEAN:
					return boolean.class;
				case Type.BYTE:
					return byte.class;
				case Type.SHORT:
					return short.class;
				case Type.CHAR:
					return char.class;
				case Type.INT:
					return int.class;
				case Type.LONG:
					return long.class;
				case Type.FLOAT:
					return float.class;
				case Type.DOUBLE:
					return double.class;
				case Type.VOID:
					return void.class;
				case Type.METHOD:
				default:
					throw new IllegalArgumentException();
			}
		} else if (cls == String[].class) {
			String[] s = (String[]) val;
			//noinspection rawtypes
			return Enum.valueOf((Class) Class.forName(s[0]), s[1]);
		} else if (val instanceof AnnotationNode) {
			AnnotationNode ann = (AnnotationNode) val;
			//noinspection unchecked
			return makeReal(ann, (Class<? extends Annotation>) Class.forName(Type.getType(ann.desc).getClassName()));
		} else if (val instanceof List) {
			List<?> list = (List<?>) val;

			Class<?> componentType = type.getComponentType();
			Object[] arr = (Object[]) Array.newInstance(componentType, list.size());
			for (int i = 0, listSize = list.size(); i < listSize; i++) {
				Object o = list.get(i);
				arr[i] = decodeAnnotationVal(o, componentType);
			}
			return arr;
		} else {
			throw new IllegalArgumentException("Invalid Annotation value");
		}
	}

	static boolean annEqImpl(Map<String, Object> data, Class<?> annotationClass, Object otherAnn) {
		if (!annotationClass.isInstance(otherAnn)) {
			return false;
		}
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			try {
				Method method = annotationClass.getMethod(entry.getKey(), ArrayUtils.EMPTY_CLASS_ARRAY);
				Object otherVal = method.invoke(otherAnn, ArrayUtils.EMPTY_OBJECT_ARRAY);
				if (!Objects.deepEquals(entry.getValue(), otherVal)) {
					return false;
				}
			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				throw JavaUtils.throwUnchecked(e);
			}
		}
		return true;
	}

	static int annHashCodeImpl(Class<?> annotationClass, Map<String, Object> data) {
		int hash = 0;

		for (Method method : annotationClass.getDeclaredMethods()) {
			String key = method.getName();
			Object val = data.get(key);
			if (val == null) {
				val = method.getDefaultValue();
			}
			int valHash = JavaUtils.hashCode(val);
			int keyHash = key.hashCode();
			hash += (127 * keyHash) ^ valHash;
		}

		return hash;
	}

	static String annToStringImpl(Class<?> annotationClass, Map<String, Object> data) {
		StringBuilder sb = new StringBuilder();
		sb.append('@')
				.append(annotationClass.getName())
				.append('(');

		Iterator<Map.Entry<String, Object>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			sb.append(entry.getKey())
					.append('=')
					.append(JavaUtils.toString(entry.getKey()));

			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(')');

		return sb.toString();
	}

	/**
	 * <p>Checks if the given annotation is present or any of the class' fields or methods.</p>
	 *
	 * @param clazz      the class to search in
	 * @param annotation the annotation to find
	 * @return true if the annotation was found on any field or method
	 */
	public static boolean hasMemberAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		String desc = Type.getDescriptor(annotation);

		List<FieldNode> fields = clazz.fields;
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = fields.size(); i < len; ++i) {
			FieldNode field = fields.get(i);
			if (getRawAnnotation(field.visibleAnnotations, field.invisibleAnnotations, desc) != null) {
				return true;
			}
		}
		List<MethodNode> methods = clazz.methods;
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = methods.size(); i < len; ++i) {
			MethodNode method = methods.get(i);
			if (getRawAnnotation(method.visibleAnnotations, method.invisibleAnnotations, desc) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>Gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given field.</p>
	 *
	 * @param field the field
	 * @param ann   the annotation class
	 * @return the AnnotationNode or null
	 */
	@Nullable
	public static AnnotationNode getRawAnnotation(FieldNode field, Class<? extends Annotation> ann) {
		return getRawAnnotation(field.visibleAnnotations, field.invisibleAnnotations, Type.getDescriptor(ann));
	}

	/**
	 * <p>Get the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given field.</p>
	 * @param field the field
	 * @param annotationClass the internal name of the annotation class
	 * @return the AnnotationNode or null
	 */
	@Nullable
	public static AnnotationNode getRawAnnotation(FieldNode field, String annotationClass) {
		return getRawAnnotation(field.visibleAnnotations, field.invisibleAnnotations, getDescriptor(annotationClass));
	}

	/**
	 * <p>Gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given class.</p>
	 *
	 * @param clazz the class
	 * @param ann   the annotation class
	 * @return the AnnotationNode or null
	 */
	@Nullable
	public static AnnotationNode getRawAnnotation(ClassNode clazz, Class<? extends Annotation> ann) {
		return getRawAnnotation(clazz.visibleAnnotations, clazz.invisibleAnnotations, Type.getDescriptor(ann));
	}

	/**
	 * <p>Get the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given class.</p>
	 * @param clazz the class
	 * @param annotationClass the internal name of the annotation class
	 * @return the AnnotationNode or null
	 */
	@Nullable
	public static AnnotationNode getRawAnnotation(ClassNode clazz, String annotationClass) {
		return getRawAnnotation(clazz.visibleAnnotations, clazz.invisibleAnnotations, getDescriptor(annotationClass));
	}

	/**
	 * <p>Gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given method.</p>
	 *
	 * @param method the method
	 * @param ann   the annotation class
	 * @return the AnnotationNode or null
	 */
	@Nullable
	public static AnnotationNode getRawAnnotation(MethodNode method, Class<? extends Annotation> ann) {
		return getRawAnnotation(method.visibleAnnotations, method.invisibleAnnotations, Type.getDescriptor(ann));
	}

	/**
	 * <p>Get the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given method.</p>
	 * @param method the method
	 * @param annotationClass the internal name of the annotation class
	 * @return the AnnotationNode or null
	 */
	@Nullable
	public static AnnotationNode getRawAnnotation(MethodNode method, String annotationClass) {
		return getRawAnnotation(method.visibleAnnotations, method.invisibleAnnotations, getDescriptor(annotationClass));
	}

	@Nullable
	static AnnotationNode getRawAnnotation(List<AnnotationNode> visAnn, List<AnnotationNode> invisAnn, String desc) {
		AnnotationNode node = findAnnotation(visAnn, desc);
		return node == null ? findAnnotation(invisAnn, desc) : node;
	}

	@Nullable
	private static AnnotationNode findAnnotation(@Nullable List<AnnotationNode> annotations, String annotationDescriptor) {
		if (annotations == null) {
			return null;
		}
		// avoid generating Iterator garbage
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = annotations.size(); i < len; ++i) {
			AnnotationNode node = annotations.get(i);
			if (node.desc.equals(annotationDescriptor)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * <p>Check if the given annotation is present on the given field.</p>
	 *
	 * @param field      the field
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(FieldNode field, Class<? extends Annotation> annotation) {
		return getRawAnnotation(field, annotation) != null;
	}

	/**
	 * <p>Check if the given annotation is present on this field.</p>
	 *
	 * @param field      the field
	 * @param annotationClass the internal name of the annotation class
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(FieldNode field, String annotationClass) {
		return getRawAnnotation(field, annotationClass) != null;
	}

	/**
	 * <p>Check if the given annotation is present on this class.</p>
	 *
	 * @param clazz      the class
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		return getRawAnnotation(clazz, annotation) != null;
	}

	/**
	 * <p>Check if the given annotation is present on this class.</p>
	 *
	 * @param clazz      the class
	 * @param annotationClass the internal name of the annotation class
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(ClassNode clazz, String annotationClass) {
		return getRawAnnotation(clazz, annotationClass) != null;
	}

	/**
	 * <p>Check if the given annotation is present on this method.</p>
	 *
	 * @param method     the method
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(MethodNode method, Class<? extends Annotation> annotation) {
		return getRawAnnotation(method, annotation) != null;
	}

	/**
	 * <p>Check if the given annotation is present on this method.</p>
	 *
	 * @param method the method
	 * @param annotationClass the internal name of the annotation class
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(MethodNode method, String annotationClass) {
		return getRawAnnotation(method, annotationClass) != null;
	}

	/**
	 * <p>Gets the given property from the annotation or {@code defaultValue} if it is not present.</p>
	 * <p>This method does not take the {@code default} value for this property into account.
	 * If you need those, use {@link #getAnnotationProperty(org.objectweb.asm.tree.AnnotationNode, String, Class)} instead.</p>
	 * <p>Annotation values are not unpacked from their ASM representation so they appear as specified in {@link org.objectweb.asm.tree.AnnotationNode#values}.
	 * Enum constants are an exception.</p>
	 *
	 * @param ann          the AnnotationNode
	 * @param key          the name of the property to get
	 * @return an Optional representing the property
	 */
	public static <T> Optional<T> getAnnotationProperty(AnnotationNode ann, String key) {
		List<Object> data = ann.values;
		int len;
		if (data == null || (len = data.size()) == 0) {
			return Optional.absent();
		}
		for (int i = 0; i < len; i += 2) {
			if (data.get(i).equals(key)) {
				//noinspection unchecked
				return Optional.of((T) unwrapAnnotationValue(data.get(i + 1)));
			}
		}
		return Optional.absent();
	}

	/**
	 * <p>Retrieves the given property from the annotation or any default value specified in the annotation class.</p>
	 *
	 * @param ann      the AnnotationNode
	 * @param key      the name of the property to get
	 * @param annClass the class of the annotation
	 * @param <T>      the type of the property
	 * @return the value of the property or the default value specified in the annotation class
	 * @throws java.util.NoSuchElementException if this annotation doesn't have this property
	 */
	public static <T> T getAnnotationProperty(AnnotationNode ann, final String key, final Class<? extends Annotation> annClass) {
		Optional<T> result = getAnnotationProperty(ann, key);
		return result.or(new Supplier<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T get() {
				try {
					return (T) annClass.getMethod(key).getDefaultValue();
				} catch (NoSuchMethodException e) {
					throw new NoSuchElementException("Annotation property " + key + " not present in class " + annClass.getName());
				}
			}
		});
	}

	private static Object unwrapAnnotationValue(Object v) {
		if (v instanceof String[]) {
			String[] data = (String[]) v;
			String className = Type.getType(data[0]).getClassName();
			try {
				return Enum.valueOf(Class.forName(className).asSubclass(Enum.class), data[1]);
			} catch (ClassNotFoundException e) {
				throw JavaUtils.throwUnchecked(e);
			}
		} else {
			return v;
		}
	}

	/**
	 * <p>Check if the given AnnotationNode has the given property.</p>
	 * @param ann the AnnotationNode
	 * @param key the name of the property to check
	 * @return true if the property is present
	 */
	public static boolean hasAnnotationProperty(AnnotationNode ann, String key) {
		List<Object> data = ann.values;
		int len;
		if (data == null || (len = data.size()) == 0) {
			return false;
		}
		for (int i = 0; i < len; i += 2) {
			if (data.get(i).equals(key)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * <p>Checks if the given {@link org.objectweb.asm.Type} represents a primitive type or the void type.</p>
	 *
	 * @param type the type
	 * @return true if the {@code Type} represents a primitive type or void
	 */
	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.ARRAY && type.getSort() != Type.OBJECT && type.getSort() != Type.METHOD;
	}

	/**
	 * <p>Checks if the given {@code Type} represents a primitive wrapper such as {@code Integer} or the {@code Void} type.</p>
	 * @param type
	 * @return
	 */
	public static boolean isPrimitiveWrapper(Type type) {
		return unboxedType(type) != type;
	}

	public static Type unboxedType(Type wrapper) {
		switch (wrapper.getInternalName()) {
			case "java/lang/Void":
				return Type.VOID_TYPE;
			case "java/lang/Boolean":
				return Type.BOOLEAN_TYPE;
			case "java/lang/Byte":
				return Type.BYTE_TYPE;
			case "java/lang/Short":
				return Type.SHORT_TYPE;
			case "java/lang/Character":
				return Type.CHAR_TYPE;
			case "java/lang/Integer":
				return Type.INT_TYPE;
			case "java/lang/Long":
				return Type.LONG_TYPE;
			case "java/lang/Float":
				return Type.FLOAT_TYPE;
			case "java/lang/Double":
				return Type.DOUBLE_TYPE;
			default:
				return wrapper;
		}
	}

	public static Type boxedType(Type primitive) {
		switch (primitive.getSort()) {
			case Type.VOID:
				return Type.getType(Void.class);
			case Type.BOOLEAN:
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
				return primitive;
		}
	}

	/**
	 * <p>Counts the number of arguments that a method with the given Type needs.</p>
	 * <p>The Type must be of Type {@link Type#METHOD}.</p>
	 *
	 * @param type the method type
	 * @return the number of arguments of the method
	 */
	public static int argumentCount(Type type) {
		checkArgument(type.getSort() == Type.METHOD);
		return argumentCount(type.getDescriptor());
	}

	/**
	 * <p>Counts the number of arguments that a method with the given descriptor needs.</p>
	 * <p>The descriptor must be a method descriptor.</p>
	 *
	 * @param methodDesc the method descriptor
	 * @return the number of arguments of the method
	 */
	public static int argumentCount(String methodDesc) {
		int off = 1; // skip initial '('
		int size = 0;
		while (true) {
			char c = methodDesc.charAt(off++);
			if (c == ')') { // end of descriptor
				return size;
			} else if (c == 'L') {
				// skip over the object name
				off = methodDesc.indexOf(';', off) + 1;
				++size;
			} else if (c != '[') { // ignore array braces
				++size;
			}
		}
	}

	/**
	 * <p>Create a new {@link org.objectweb.asm.Type} that represents an array with {@code dimensions} dimensions and the
	 * component type {@code elementType}.</p>
	 *
	 * @param elementType the component type of the array type to create, must not be a Method type.
	 * @param dimensions  the number of dimensions to create
	 * @return a new Type representing the array type.
	 */
	public static Type asArray(Type elementType, int dimensions) {
		int sort = elementType.getSort();
		checkArgument(sort != Type.METHOD, "Type must not be method type");

		if (sort == Type.ARRAY) {
			dimensions += elementType.getDimensions();
			elementType = elementType.getElementType();
		}

		StringBuilder b = new StringBuilder();
		for (int i = 0; i < dimensions; ++i) {
			b.append('[');
		}
		b.append(elementType.getDescriptor());
		return Type.getObjectType(b.toString());
	}
}
