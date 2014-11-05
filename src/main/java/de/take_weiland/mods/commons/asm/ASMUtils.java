package de.take_weiland.mods.commons.asm;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.OverrideSetter;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Annotation;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.*;

public final class ASMUtils {

	private ASMUtils() { }

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
	 * this method will create the default constructor.</p>
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
			rootCtsrs = Arrays.asList(method);
		}
		for (MethodNode cstr : rootCtsrs) {
			code.insertAfter(cstr.instructions, findFirst(cstr.instructions, INVOKESPECIAL));
		}
	}

	/**
	 * <p>Add the given code to the static-initializer of the given class.</p>
	 * <p>If the static-initializer is not present, this method will create it.</p>
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
			public boolean apply(MethodNode input) {
				return isConstructor(input);
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

	private static boolean isAccessibleFrom(ClassNode accessingClass, ClassNode targetClass, int targetAccess) {
		// public methods are reachable from everywhere
		if ((targetAccess & ACC_PUBLIC) == ACC_PUBLIC) {
			return true;
		}

		// classes can access their own methods
		if (accessingClass.name.equals(targetClass.name)) {
			return true;
		}

		// private methods are only reachable from within the same class
		if ((targetAccess & ACC_PRIVATE) == ACC_PRIVATE) {
			return false;
		}

		// method can only be protected or package-private at this point
		// if package is equal, it's accessible
		if (getPackage(accessingClass.name).equals(getPackage(targetClass.name))) {
			return true;
		}

		// if method is protected, check the class hierarchy
		return (targetAccess & ACC_PROTECTED) == ACC_PROTECTED && ClassInfo.of(targetClass).isAssignableFrom(ClassInfo.of(targetClass));
	}

	private static String getPackage(String internalName) {
		int idx = internalName.lastIndexOf('/');
		return idx == -1 ? "" : internalName.substring(0, idx);
	}

	/**
	 * <p>Useful if you need to add code that is called, whenever a new instance of the class is created, no matter through which constructor.</p>
	 *
	 * @param clazz the class
	 * @return all root constructors
	 */
	public static List<MethodNode> getRootConstructors(ClassNode clazz) {
		List<MethodNode> roots = Lists.newArrayList();

		cstrs:
		for (MethodNode method : getConstructors(clazz)) {
			AbstractInsnNode insn = method.instructions.getFirst();
			do {
				if (insn.getOpcode() == INVOKESPECIAL && ((MethodInsnNode) insn).owner.equals(clazz.name)) {
					continue cstrs;
				}
				insn = insn.getNext();
			} while (insn != null);
			roots.add(method);
		}
		return roots;
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
			public boolean apply(MethodNode method) {
				return hasAnnotation(method, annotation);
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
			public boolean apply(FieldNode field) {
				return hasAnnotation(field, annotation);
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

		AnnotationNode overrideSetter = getAnnotation(getter, OverrideSetter.class);
		if (overrideSetter != null) {
			setterName = getAnnotationProperty(overrideSetter, "value");
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
	 * Walks {@code n} steps forwards in the InsnList of the given instruction.
	 *
	 * @param insn the starting point
	 * @param n    how many steps to move forwards
	 * @return the instruction {@code n} steps forwards
	 * @throws java.lang.IndexOutOfBoundsException if the list ends before n steps have been walked
	 */
	public static AbstractInsnNode getNext(AbstractInsnNode insn, int n) {
		for (int i = 0; i < n; ++i) {
			insn = insn.getNext();
			if (insn == null) {
				throw new IndexOutOfBoundsException();
			}
		}
		return insn;
	}

	/**
	 * Walks {@code n} steps backwards in the InsnList of the given instruction.
	 *
	 * @param insn the starting point
	 * @param n    how many steps to move backwards
	 * @return the instruction {@code n} steps backwards
	 * @throws java.lang.IndexOutOfBoundsException if the list ends before n steps have been walked
	 */
	public static AbstractInsnNode getPrevious(AbstractInsnNode insn, int n) {
		for (int i = 0; i < n; ++i) {
			insn = insn.getPrevious();
			if (insn == null) {
				throw new IndexOutOfBoundsException();
			}
		}
		return insn;
	}

	/**
	 * Creates a new InsnList that contains clones of the instructions going from {@code from} to {@code to}.
	 *
	 * @param insns the InsnList
	 * @param from  the first node to clone, must be in the InsnList (inclusive)
	 * @param to    the last node to clone, must be in the InsnList (inclusive)
	 * @return the cloned list
	 */
	public static InsnList clone(InsnList insns, AbstractInsnNode from, AbstractInsnNode to) {
		InsnList clone = new InsnList();
		Map<LabelNode, LabelNode> labels = labelCloneMap(insns.getFirst());

		AbstractInsnNode fence = to.getNext();
		AbstractInsnNode current = from;
		do {
			clone.add(current.clone(labels));
			current = current.getNext();
			if (current == fence) {
				break;
			}
		} while (true);
		return clone;
	}

	public static InsnList clone(InsnList insns) {
		return clone(insns, labelCloneMap(insns.getFirst()));
	}

	public static InsnList clone(InsnList list, Map<LabelNode, LabelNode> map) {
		InsnList cloned = new InsnList();
		AbstractInsnNode current = list.getFirst();
		while (current != null) {
			cloned.add(current.clone(map));
			current = current.getNext();
		}
		return cloned;
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractInsnNode> T clone(T insn) {
		return (T) insn.clone(labelCloneMap(findFirstInList(insn)));
	}

	private static AbstractInsnNode findFirstInList(AbstractInsnNode insn) {
		while (insn.getPrevious() != null) {
			insn = insn.getPrevious();
		}
		return insn;
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
	 * convert the given binary name (e.g. {@code java.lang.Object$Subclass}) to an internal name (e.g. {@code java/lang/Object$Subclass})
	 *
	 * @param binaryName the binary name
	 * @return the internal name
	 */
	public static String internalName(String binaryName) {
		return binaryName.replace('.', '/');
	}

	/**
	 * convert the given internal name to a binary name (opposite of {@link #internalName(String)}
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
	 * @see org.objectweb.asm.Type#getDescriptor()
	 * @return the descriptor
	 */
	public static String getDescriptor(String internalName) {
		return "L" + internalName + ";";
	}

	private static IClassNameTransformer nameTransformer;
	private static boolean nameTransChecked = false;

	/**
	 * get the active {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 *
	 * @return the active transformer, or null if none
	 */
	public static IClassNameTransformer getClassNameTransformer() {
		if (!nameTransChecked) {
			nameTransformer = Iterables.getOnlyElement(Iterables.filter(Launch.classLoader.getTransformers(), IClassNameTransformer.class), null);
			nameTransChecked = true;
		}
		return nameTransformer;
	}

	/**
	 * transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 *
	 * @param untransformedName the un-transformed name of the class
	 * @return the transformed name of the class
	 */
	public static String transformName(String untransformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? untransformedName : t.remapClassName(binaryName(untransformedName)));
	}

	/**
	 * un-transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 *
	 * @param transformedName the transformed name of the class
	 * @return the un-transformed name of the class
	 */
	public static String untransformName(String transformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? transformedName : t.unmapClassName(binaryName(transformedName)));
	}

	// *** Misc Utils *** //

	/**
	 * equivalent to {@link #getClassNode(String, int)} with no ClassReader flags
	 */
	public static ClassNode getClassNode(String name) {
		return getClassNode(name, 0);
	}

	/**
	 * gets a {@link org.objectweb.asm.tree.ClassNode} for the given class name
	 *
	 * @param name        the class to load
	 * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
	 * @return a ClassNode
	 * @throws MissingClassException if the class couldn't be found or can't be loaded as raw-bytes
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
	 * equivalent to {@link #getClassNode(byte[], int)} with no ClassReader flags
	 */
	public static ClassNode getClassNode(byte[] bytes) {
		return getClassNode(bytes, 0);
	}

	/**
	 * gets a {@link org.objectweb.asm.tree.ClassNode} representing the class described by the given bytes
	 *
	 * @param bytes       the raw bytes describing the class
	 * @param readerFlags the the flags to pass to the {@link org.objectweb.asm.ClassReader}
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
	 * equivalent to {@link #getClassNode(String, int)} with all skip flags set ({@code ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES})
	 */
	public static ClassNode getThinClassNode(String name) {
		return getClassNode(name, THIN_FLAGS);
	}

	/**
	 * equivalent to {@link #getClassNode(byte[], int)} with all skip flags set ({@code ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES})
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
	 * <p>Finds the given annotation on the given Class or on any Field or Method in the class.</p>
	 *
	 * @param clazz      the class to search in
	 * @param annotation the annotation to find
	 * @return true if the annotation was found on the Class or any Field or Method
	 */
	public static boolean hasMemberAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		String desc = Type.getDescriptor(annotation);

		List<FieldNode> fields = clazz.fields;
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = fields.size(); i < len; ++i) {
			FieldNode field = fields.get(i);
			if (getAnnotation(field.visibleAnnotations, field.invisibleAnnotations, desc) != null) {
				return true;
			}
		}
		List<MethodNode> methods = clazz.methods;
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = methods.size(); i < len; ++i) {
			MethodNode method = methods.get(i);
			if (getAnnotation(method.visibleAnnotations, method.invisibleAnnotations, desc) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given field</p>
	 *
	 * @param field the field
	 * @param ann   the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(FieldNode field, Class<? extends Annotation> ann) {
		return getAnnotation(field.visibleAnnotations, field.invisibleAnnotations, Type.getDescriptor(ann));
	}

	public static AnnotationNode getAnnotation(FieldNode field, String annotationClass) {
		return getAnnotation(field.visibleAnnotations, field.invisibleAnnotations, getDescriptor(annotationClass));
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given class
	 *
	 * @param clazz the class
	 * @param ann   the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(ClassNode clazz, Class<? extends Annotation> ann) {
		return getAnnotation(clazz.visibleAnnotations, clazz.invisibleAnnotations, Type.getDescriptor(ann));
	}

	public static AnnotationNode getAnnotation(ClassNode clazz, String annotationClass) {
		return getAnnotation(clazz.visibleAnnotations, clazz.invisibleAnnotations, getDescriptor(annotationClass));
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given annotation class, if present on the given method
	 *
	 * @param method the method
	 * @param ann    the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(MethodNode method, Class<? extends Annotation> ann) {
		return getAnnotation(method.visibleAnnotations, method.invisibleAnnotations, Type.getDescriptor(ann));
	}

	public static AnnotationNode getAnnotation(MethodNode method, String annotationClass) {
		return getAnnotation(method.visibleAnnotations, method.invisibleAnnotations, getDescriptor(annotationClass));
	}

	static AnnotationNode getAnnotation(List<AnnotationNode> visAnn, List<AnnotationNode> invisAnn, String desc) {
		AnnotationNode node = findAnnotation(visAnn, desc);
		return node == null ? findAnnotation(invisAnn, desc) : node;
	}

	private static AnnotationNode findAnnotation(List<AnnotationNode> annotations, String annotationDescriptor) {
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
	 * <p>Check if the given annotation class is present on this field.</p>
	 *
	 * @param field      the field
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(FieldNode field, Class<? extends Annotation> annotation) {
		return getAnnotation(field, annotation) != null;
	}

	/**
	 * <p>Check if the given annotation class is present on this field.</p>
	 *
	 * @param field      the field
	 * @param annotationClass the internal name of the annotation class
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(FieldNode field, String annotationClass) {
		return getAnnotation(field, annotationClass) != null;
	}

	/**
	 * <p>Check if the given annotation class is present on this class.</p>
	 *
	 * @param clazz      the class
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		return getAnnotation(clazz, annotation) != null;
	}

	/**
	 * <p>Check if the given annotation class is present on this class.</p>
	 *
	 * @param clazz      the class
	 * @param annotationClass the internal name of the annotation class
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(ClassNode clazz, String annotationClass) {
		return getAnnotation(clazz, annotationClass) != null;
	}

	/**
	 * <p>Check if the given annotation class is present on this method.</p>
	 *
	 * @param method     the method
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(MethodNode method, Class<? extends Annotation> annotation) {
		return getAnnotation(method, annotation) != null;
	}

	/**
	 * <p>Check if the given annotation class is present on this method.</p>
	 *
	 * @param method the method
	 * @param annotationClass the internal name of the annotation class
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(MethodNode method, String annotationClass) {
		return getAnnotation(method, annotationClass) != null;
	}

	/**
	 * <p>Retrieves the given property from the annotation or {@code null} if it is not present.</p>
	 * <p>This method does not take the {@code default} value for this property into account.
	 * If you need those, use {@link #getAnnotationProperty(org.objectweb.asm.tree.AnnotationNode, String, Class)} instead.</p>
	 *
	 * @param ann the AnnotationNode
	 * @param key the name of the property to get
	 * @param <T> the type of the property
	 * @return the value of the property or null if the property is not present
	 */
	public static <T> T getAnnotationProperty(AnnotationNode ann, String key) {
		return getAnnotationProperty(ann, key, (T) null);
	}

	/**
	 * <p>Retrieves the given property from the annotation or {@code defaultValue} if it is not present.</p>
	 * <p>This method does not take the {@code default} value for this property into account.
	 * If you need those, use {@link #getAnnotationProperty(org.objectweb.asm.tree.AnnotationNode, String, Class)} instead.</p>
	 *
	 * @param ann          the AnnotationNode
	 * @param key          the name of the property to get
	 * @param defaultValue a default value, in case the property is not present.
	 * @param <T>          the type of the property
	 * @return the value of the property or {@code defaultValue} if the property is not present
	 */
	public static <T> T getAnnotationProperty(AnnotationNode ann, String key, T defaultValue) {
		List<Object> data = ann.values;
		int len;
		if (data == null || (len = data.size()) == 0) {
			return defaultValue;
		}
		for (int i = 0; i < len; i += 2) {
			if (data.get(i).equals(key)) {
				//noinspection unchecked
				return (T) unwrapAnnotationValue(data.get(i + 1));
			}
		}
		return defaultValue;
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
	public static <T> T getAnnotationProperty(AnnotationNode ann, String key, Class<? extends Annotation> annClass) {
		T result = getAnnotationProperty(ann, key, (T) null);
		if (result == null) {
			try {
				//noinspection unchecked
				return (T) annClass.getMethod(key).getDefaultValue();
			} catch (NoSuchMethodException e) {
				throw new NoSuchElementException("Annotation property " + key + "missing");
			}
		} else {
			return result;
		}
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

	public static boolean isPrimitiveWrapper(Type type) {
		return unboxedType(type) != type;
	}

	public static Type unboxedType(Type wrapper) {
		switch (wrapper.getInternalName()) {
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
