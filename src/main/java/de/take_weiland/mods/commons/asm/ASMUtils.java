package de.take_weiland.mods.commons.asm;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.primitives.Primitives;
import de.take_weiland.mods.commons.OverrideSetter;
import de.take_weiland.mods.commons.fastreflect.Fastreflect;
import net.minecraft.launchwrapper.IClassNameTransformer;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static de.take_weiland.mods.commons.internal.SevenCommons.CLASSLOADER;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;

public final class ASMUtils {

	private ASMUtils() { }

	// *** bytecode analyzing helpers *** //

	/**
	 * finds the last return instruction in the given method.
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

	@Deprecated
	public static AbstractInsnNode findLast(MethodNode method, int opcode) {
		return findLast(method.instructions, opcode);
	}

	public static AbstractInsnNode findLast(InsnList insns, int opcode) {
		AbstractInsnNode node = insns.getLast();
		do {
			if (node.getOpcode() == opcode) {
				return node;
			}
			node = node.getPrevious();
		} while (node != null);
		return null;
	}

	public static AbstractInsnNode findFirst(InsnList insns, int opcode) {
		AbstractInsnNode node = insns.getFirst();
		do {
			if (node.getOpcode() == opcode) {
				return node;
			}
			node = node.getNext();
		} while (node != null);
		return null;
	}

	@Deprecated
	public static AbstractInsnNode findFirst(MethodNode method, int opcode) {
		return findFirst(method.instructions, opcode);
	}

	// *** method finding helpers *** //

	/**
	 * find the method with the given name. If multiple methods with the same parameters exist, the first one will be returned
	 * @param clazz the class
	 * @param name the method name to search for
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
	 * find the method with the given name and method descriptor.
	 * @param clazz the class
	 * @param name the method name to search for
	 * @param desc the method descriptor to search for
	 * @return the method with the given name and descriptor or null if no such method is found
	 * @see org.objectweb.asm.Type#getMethodDescriptor
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
	 * Find the method with the given name. It is automatically chosen between MCP and SRG name, depending on if this code is running in a development environment.
	 * @param clazz the class
	 * @param mcpName the MCP name of the method (e.g. {@code updateEntity})
	 * @param srgName the SRG name of the method (e.g. {@code func_70316_g}
	 * @return the first method with the given name or null if no such method is found
	 */
	public static MethodNode findMinecraftMethod(ClassNode clazz, String mcpName, String srgName) {
		return findMethod(clazz, MCPNames.use() ? mcpName : srgName);
	}

	/**
	 * find the method with the given name and descriptor. It is automatically chosen between MCP and SRG name, depending on if this code is running in a development environment.
	 * @param clazz the class
	 * @param mcpName the MCP name of the method (e.g. {@code updateEntity})
	 * @param srgName the SRG name of the method (e.g. {@code func_70316_g}
	 * @param desc the method descriptor of the method
	 * @return the method or null if no such method is found
	 * @see org.objectweb.asm.Type#getMethodDescriptor
	 */
	public static MethodNode findMinecraftMethod(ClassNode clazz, String mcpName, String srgName, String desc) {
		return findMethod(clazz, MCPNames.use() ? mcpName : srgName, desc);
	}

	/**
	 * like {@link #findMethod(org.objectweb.asm.tree.ClassNode, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMethod(ClassNode clazz, String name) {
		MethodNode m = findMethod(clazz, name);
		if (m == null) {
			throw MissingMethodException.create(name, clazz.name);
		}
		return m;
	}

	/**
	 * like {@link #findMethod(org.objectweb.asm.tree.ClassNode, String, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMethod(ClassNode clazz, String name, String desc) {
		MethodNode m = findMethod(clazz, name, desc);
		if (m == null) {
			throw MissingMethodException.withDesc(name, desc, clazz.name);
		}
		return m;
	}

	/**
	 * like {@link #findMinecraftMethod(org.objectweb.asm.tree.ClassNode, String, String, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMinecraftMethod(ClassNode clazz, String mcpName, String srgName, String desc) {
		MethodNode m = findMinecraftMethod(clazz, mcpName, srgName, desc);
		if (m == null) {
			throw MissingMethodException.withDesc(mcpName, srgName, desc, clazz.name);
		}
		return m;
	}

	/**
	 * like {@link #findMinecraftMethod(org.objectweb.asm.tree.ClassNode, String, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMinecraftMethod(ClassNode clazz, String mcpName, String srgName) {
		MethodNode m = findMinecraftMethod(clazz, mcpName, srgName);
		if (m == null) {
			throw MissingMethodException.create(mcpName, srgName, clazz.name);
		}
		return m;
	}

	private static final Predicate<MethodNode> IS_CONSTRUCTOR = Predicates.compose(Predicates.equalTo("<init>"), new Function<MethodNode, String>() {
		@Override
		public String apply(MethodNode input) {
			return input.name;
		}
	});

	/**
	 * <p>get all constructors of the given ClassNode</p>
	 * <p>The returned collection is a live-view, so if new constructors get added, they will be present in the returned collection immediately</p>
	 * @param clazz the class
	 * @return all constructors
	 */
	public static Collection<MethodNode> getConstructors(ClassNode clazz) {
		return Collections2.filter(clazz.methods, IS_CONSTRUCTOR);
	}

	/**
	 * <p>Get all constructors, which don't call another constructor of the same class.</p>
	 * <p>Useful if you need to add code that is called, whenever a new instance of the class is created, no matter through which constructor.</p>
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
	 * <p>Get all methods in the given class which have the given Annotation</p>
	 * @param clazz the ClassNode
	 * @param annotation the annotation to search for
	 * @return a Collection containing all methods in the class which have the given Annotation
	 */
	public static Collection<MethodNode> methodsWith(ClassNode clazz, final Class<? extends Annotation> annotation) {
		return Collections2.filter(clazz.methods, new Predicate<MethodNode>() {
			@Override
			public boolean apply(MethodNode method) {
				return hasAnnotation(method, annotation);
			}
		});
	}

	/**
	 * <p>Get all fields in the given class which have the given Annotation</p>
	 * @param clazz the ClassNode
	 * @param annotation the annotation to search for
	 * @return a Collection containing all fields in the class which have the given Annotation
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

	public static MethodNode findSetter(ClassNode clazz, MethodNode getter) {
		Type type = getterType(getter);

		String setterName;

		AnnotationNode overrideSetter = getAnnotationRaw(getter, OverrideSetter.class);
		if (overrideSetter != null) {
			setterName = getAnnotationProperty(overrideSetter, "value");
		} else {
			if (getter.name.startsWith("get")) {
				setterName = "set" + getter.name.substring(3);
			} else if (getter.name.startsWith("is")) {
				setterName = "set" + getter.name.substring(2);
			} else if (Strings.nullToEmpty(clazz.sourceFile).endsWith(".scala")) {
				setterName = getter.name + "_$eq";
			} else {
				setterName = getter.name;
			}
		}
		String setterDesc = Type.getMethodDescriptor(Type.VOID_TYPE, type);
		return findMethod(clazz, setterName, setterDesc);
	}

	public static boolean matches(AbstractInsnNode a, AbstractInsnNode b) {
		return matches(a, b, false);
	}

	public static boolean matches(AbstractInsnNode a, AbstractInsnNode b, boolean lenient) {
		if (a.getOpcode() != b.getOpcode()) {
			return false;
		}
		if (lenient) {
			return true;
		}
		switch (a.getType()) {
			case INSN:
			case JUMP_INSN:
			case LABEL:
			case FRAME:
			case LINE:
				return true;
			case INT_INSN:
				return intInsnEq((IntInsnNode) a, (IntInsnNode) b);
			case VAR_INSN:
				return varInsnEq((VarInsnNode) a, (VarInsnNode) b);
			case TYPE_INSN:
				return typeInsnEq((TypeInsnNode) a, (TypeInsnNode) b);
			case FIELD_INSN:
				return fieldInsnEq((FieldInsnNode) a, (FieldInsnNode) b);
			case METHOD_INSN:
				return methodInsnEq((MethodInsnNode) a, (MethodInsnNode) b);
			case LDC_INSN:
				return ldcInsnEq((LdcInsnNode) a, (LdcInsnNode) b);
			case IINC_INSN:
				return iincInsnEq((IincInsnNode) a, (IincInsnNode) b);
			case TABLESWITCH_INSN:
				return tableSwitchEq((TableSwitchInsnNode) a, (TableSwitchInsnNode) b);
			case LOOKUPSWITCH_INSN:
				return lookupSwitchEq((LookupSwitchInsnNode) a, (LookupSwitchInsnNode) b);
			case MULTIANEWARRAY_INSN:
				return multiANewArrayEq((MultiANewArrayInsnNode) a, (MultiANewArrayInsnNode) b);
			case INVOKE_DYNAMIC_INSN:
				return invokeDynamicEq((InvokeDynamicInsnNode) a, (InvokeDynamicInsnNode) b);
			default:
				throw new AssertionError();
		}
	}

	private static boolean intInsnEq(IntInsnNode a, IntInsnNode b) {
		return a.operand == b.operand;
	}

	private static boolean varInsnEq(VarInsnNode a, VarInsnNode b) {
		return a.var == b.var;
	}

	private static boolean typeInsnEq(TypeInsnNode a, TypeInsnNode b) {
		return a.desc.equals(b.desc);
	}

	private static boolean fieldInsnEq(FieldInsnNode a, FieldInsnNode b) {
		return a.name.equals(b.name) && a.owner.equals(b.owner) && a.desc.equals(b.desc);
	}

	private static boolean methodInsnEq(MethodInsnNode a, MethodInsnNode b) {
		return a.name.equals(b.name) && a.owner.equals(b.owner) && a.desc.equals(b.desc);
	}

	private static boolean ldcInsnEq(LdcInsnNode a, LdcInsnNode b) {
		return a.cst.equals(b.cst);
	}

	private static boolean iincInsnEq(IincInsnNode a, IincInsnNode b) {
		return a.var == b.var && a.incr == b.incr;
	}

	private static boolean tableSwitchEq(TableSwitchInsnNode a, TableSwitchInsnNode b) {
		return a.min == b.min && a.max == b.max;
	}

	private static boolean lookupSwitchEq(LookupSwitchInsnNode a, LookupSwitchInsnNode b) {
		return a.keys.equals(b.keys);
	}

	private static boolean multiANewArrayEq(MultiANewArrayInsnNode a, MultiANewArrayInsnNode b) {
		return a.dims == b.dims && a.desc.equals(b.desc);
	}

	private static boolean invokeDynamicEq(InvokeDynamicInsnNode a, InvokeDynamicInsnNode b) {
		return a.name.equals(b.name)
				&& a.desc.equals(b.desc)
				&& a.bsm.equals(b.bsm)
				&& Arrays.equals(a.bsmArgs, b.bsmArgs);
	}

	/**
	 * Walks {@code n} steps forwards in the InsnList of the given instruction.
	 * @param insn the starting point
	 * @param n how many steps to move forwards
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
	 * <p>Provides identical functionality to {@link #getNext(org.objectweb.asm.tree.AbstractInsnNode, int)},
	 * but can provide constant-time performance in certain situations, as opposed to the linear-time performance of
	 * {@link #getNext(org.objectweb.asm.tree.AbstractInsnNode, int)}.</p>
	 * <p>The constant-time implementation is used if the cache of the InsnList is already created. To force that to happen,
	 * call {@link org.objectweb.asm.tree.InsnList#get(int)} once before calling this method.</p>
	 * @param list the InsnList of the instruction
	 * @param insn the instruction
	 * @param n how many steps to move forwards
	 * @return the instruction {@code n} steps forwards
	 * @throws java.lang.IndexOutOfBoundsException if the list ends before n steps have been walked
	 */
	public static AbstractInsnNode getNext(InsnList list, AbstractInsnNode insn, int n) {
		if (SCASMAccessHook.getCache(list) != null) {
			int idx = SCASMAccessHook.getIndex(insn);
			checkArgument(idx >= 0, "instruction doesn't belong to list!");
			return list.get(idx + n);
		} else {
			return getNext(insn, n);
		}
	}

	/**
	 * Walks {@code n} steps backwards in the InsnList of the given instruction.
	 * @param insn the starting point
	 * @param n how many steps to move backwards
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
	 * <p>Provides identical functionality to {@link #getPrevious(org.objectweb.asm.tree.AbstractInsnNode, int)},
	 * but can provide constant-time performance in certain situations, as opposed to the linear-time performance of
	 * {@link #getPrevious(org.objectweb.asm.tree.AbstractInsnNode, int)}.</p>
	 * <p>The constant-time implementation is used if the cache of the InsnList is already created. To force that to happen,
	 * call {@link org.objectweb.asm.tree.InsnList#get(int)} once before calling this method.</p>
	 * @param list the InsnList of the instruction
	 * @param insn the instruction
	 * @param n how many steps to move backwards
	 * @return the instruction {@code n} steps backwards
	 * @throws java.lang.IndexOutOfBoundsException if the list ends before n steps have been walked
	 */
	public static AbstractInsnNode getPrevious(InsnList list, AbstractInsnNode insn, int n) {
		if (SCASMAccessHook.getCache(list) != null) {
			int idx = SCASMAccessHook.getIndex(insn);
			checkArgument(idx >= 0, "instruction doesn't belong to list!");
			return list.get(idx - n);
		} else {
			return getPrevious(insn, n);
		}
	}

	public static CodeSearcher searchIn(InsnList insns) {
		return new CodeSearcherImpl(insns);
	}

	public static int fastIdx(InsnList list, AbstractInsnNode insn) {
		if (insn == list.getFirst()) {
			return 0;
		} else if (insn == list.getLast()) {
			return list.size() - 1;
		} else {
			return list.indexOf(insn);
		}
	}

	/**
	 * Creates a new InsnList that contains clones of the instructions going from {@code from} to {@code to}.
	 * @param insns the InsnList
	 * @param from the first node to clone, must be in the InsnList (inclusive)
	 * @param to the last node to clone, must be in the InsnList (inclusive)
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

	/**
	 * Clones the given
	 * @param insns
	 * @param from
	 * @return
	 */
	public static InsnList clone(InsnList insns, AbstractInsnNode from) {
		return clone(insns, from, insns.getLast());
	}

	public static InsnList clone(InsnList insns) {
		return clone(insns, insns.getFirst(), insns.getLast());
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

	/**
	 * @deprecated use {@link MCPNames#use()}
	 */
	@Deprecated
	public static boolean useMcpNames() {
		return MCPNames.use();
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
	 * @param binaryName the binary name
	 * @return the internal name
	 */
	public static String internalName(String binaryName) {
		return binaryName.replace('.', '/');
	}

	/**
	 * convert the given internal name to a binary name (opposite of {@link #internalName(String)}
	 * @param internalName the internal name
	 * @return the binary name
	 */
	public static String binaryName(String internalName) {
		return internalName.replace('/', '.');
	}

	@Deprecated
	public static String makeNameInternal(String name) {
		return internalName(name);
	}

	@Deprecated
	public static String undoInternalName(String name) {
		return binaryName(name);
	}

	private static IClassNameTransformer nameTransformer;
	private static boolean nameTransChecked = false;

	/**
	 * get the active {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 * @return the active transformer, or null if none
	 */
	public static IClassNameTransformer getClassNameTransformer() {
		if (!nameTransChecked) {
			nameTransformer = Iterables.getOnlyElement(Iterables.filter(CLASSLOADER.getTransformers(), IClassNameTransformer.class), null);
			nameTransChecked = true;
		}
		return nameTransformer;
	}

	/**
	 * transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 * @param untransformedName the un-transformed name of the class
	 * @return the transformed name of the class
	 */
	public static String transformName(String untransformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? untransformedName : t.remapClassName(binaryName(untransformedName)));
	}

	/**
	 * un-transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 * @param transformedName the transformed name of the class
	 * @return the un-transformed name of the class
	 */
	public static String untransformName(String transformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? transformedName : t.unmapClassName(binaryName(transformedName)));
	}

	@Deprecated
	public static String obfuscateClass(String deobfName) {
		return untransformName(deobfName);
	}

	@Deprecated
	public static String deobfuscateClass(String obfName) {
		return transformName(obfName);
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
	 * @param name the class to load
	 * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
	 * @return a ClassNode
	 * @throws MissingClassException if the class couldn't be found or can't be loaded as raw-bytes
	 */
	public static ClassNode getClassNode(String name, int readerFlags) {
		try {
			byte[] bytes = CLASSLOADER.getClassBytes(transformName(name));
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
	 * @param bytes the raw bytes describing the class
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
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given field
	 * @param field the field
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotationRaw(FieldNode field, Class<? extends Annotation> ann) {
		return getAnnotationRaw(field.visibleAnnotations, field.invisibleAnnotations, ElementType.FIELD, ann);
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given class
	 * @param clazz the class
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotationRaw(ClassNode clazz, Class<? extends Annotation> ann) {
		AnnotationNode node = getAnnotationRaw(clazz.visibleAnnotations, clazz.invisibleAnnotations, ElementType.TYPE, ann);
		if (node != null || clazz.superName == null) {
			return node;
		}

		boolean inherited = ann.isAnnotationPresent(Inherited.class);
		ClassInfo info = ClassInfo.of(clazz.superName);
		do {
//			AnnotationNode ann = info.get
		} while (false);
		if (true) return null;
		do {
			node = getAnnotationRaw(clazz.visibleAnnotations, clazz.invisibleAnnotations, ElementType.TYPE, ann);
			if (node != null) {
				return node;
			}
			if (info == null) {
				info = ClassInfo.of(clazz.superName);
			} else {
				info = info.superclass();
			}
		} while (false);
		return null;
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given method
	 * @param method the method
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotationRaw(MethodNode method, Class<? extends Annotation> ann) {
		return getAnnotationRaw(method.visibleAnnotations, method.invisibleAnnotations, ElementType.METHOD, ann);
	}

	static AnnotationNode getAnnotationRaw(List<AnnotationNode> visAnn, List<AnnotationNode> invisAnn, ElementType reqType, Class<? extends Annotation> ann) {
		Target annTarget = ann.getAnnotation(Target.class);
		if (annTarget != null && !ArrayUtils.contains(annTarget.value(), reqType)) {
			return null;
		}

		Retention ret = ann.getAnnotation(Retention.class);
		RetentionPolicy retention = ret == null ? RetentionPolicy.CLASS : ret.value();
		checkArgument(retention != RetentionPolicy.SOURCE, "Cannot check SOURCE annotations from class files!");

		List<AnnotationNode> anns = retention == RetentionPolicy.CLASS ? invisAnn : visAnn;
		if (anns == null) {
			return null;
		}
		String desc = Type.getDescriptor(ann);
		// avoid generating Iterator garbage
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = anns.size(); i < len; ++i) {
			AnnotationNode node;
			if ((node = anns.get(i)).desc.equals(desc)) {
				return node;
			}
		}
		return null;
	}

	public static <T extends Annotation> T getAnnotation(ClassNode clazz, Class<T> annotationType) {
		return compileAnnotation(getAnnotationRaw(clazz, annotationType), annotationType);
	}

	public static <T extends Annotation> T getAnnotation(FieldNode field, Class<T> annotationType) {
		return compileAnnotation(getAnnotationRaw(field, annotationType), annotationType);
	}

	public static <T extends Annotation> T getAnnotation(MethodNode method, Class<T> annotationType) {
		return compileAnnotation(getAnnotationRaw(method, annotationType), annotationType);
	}

	private static <T extends Annotation> T compileAnnotation(AnnotationNode node, Class<T> annotationType) {
		ClassWriter cw = new ClassWriter(0);
		cw.visitSource(".dynamic", null);

		String className = Fastreflect.nextDynamicClassName(ASMUtils.class.getPackage());
		String superName = Type.getInternalName(AbstractDynamicAnnotation.class);

		cw.visit(V1_6, ACC_PUBLIC, className, null, superName, new String[]{ Type.getInternalName(annotationType) });

		String cName = "<init>";
		String cDesc = getMethodDescriptor(void.class, AnnotationNode.class);

		MethodVisitor mv = cw.visitMethod(0, cName, cDesc, null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, superName, cName, cDesc);
		mv.visitInsn(RETURN);

		mv.visitMaxs(2, 2);
		mv.visitEnd();

		String asmUtils = Type.getInternalName(ASMUtils.class);
		String getAnnProp = "getAnnotationProperty";
		String getAnnPropDesc = getMethodDescriptor(Object.class, AnnotationNode.class, String.class, Object.class);

		for (Method m : annotationType.getMethods()) {
			Class<?> returnType = m.getReturnType();
			Class<?> wrappedReturnType = Primitives.wrap(returnType);

			mv = cw.visitMethod(ACC_PUBLIC, m.getName(), Type.getMethodDescriptor(m), null, null);
			mv.visitCode();

			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, superName, AbstractDynamicAnnotation.FIELD_NAME, Type.getDescriptor(AnnotationNode.class));
			mv.visitLdcInsn(m.getName());

			CodePieces.constant(m.getDefaultValue()).appendTo(mv);

			mv.visitMethodInsn(INVOKESTATIC, asmUtils, getAnnProp, getAnnPropDesc);
			mv.visitTypeInsn(CHECKCAST, Type.getInternalName(wrappedReturnType));

			if (returnType.isPrimitive()) {
				String unwrapMethod = returnType.getName() + "Value";
				mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(wrappedReturnType), unwrapMethod, getMethodDescriptor(returnType));
			}

			mv.visitInsn(Type.getType(returnType).getOpcode(IRETURN));

			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}
		cw.visitEnd();
		try {
			//noinspection unchecked
			return (T) Fastreflect.defineDynamicClass(cw.toByteArray()).newInstance();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public static <T> T getAnnotationProperty(AnnotationNode ann, String key) {
		return getAnnotationProperty(ann, key, (T) null);
	}

	public static <T> T getAnnotationProperty(AnnotationNode ann, String key, Class<? extends Annotation> annClass) {
		T result = getAnnotationProperty(ann, key, (T) null);
		if (result == null) {
			try {
				//noinspection unchecked
				return (T) annClass.getMethod(key).getDefaultValue();
			} catch (NoSuchMethodException e) {
				return null;
			}
		} else {
			return result;
		}
	}

	public static <T> T getAnnotationProperty(AnnotationNode ann, String key, T defaultValue) {
		List<Object> data = ann.values;
		int len;
		if (data == null || (len = data.size()) == 0) {
			return defaultValue;
		}
		for (int i = 0; i < len; i += 2) {
			if (data.get(i).equals(key)) {
				//noinspection unchecked
				return (T) data.get(i + 1);
			}
		}
		return defaultValue;
	}

	/**
	 * check if the given Annotation class is present on this field
	 * @param field the field
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(FieldNode field, Class<? extends Annotation> annotation) {
		return getAnnotationRaw(field, annotation) != null;
	}

	/**
	 * check if the given Annotation class is present on this class
	 * @param clazz the class
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		return getAnnotationRaw(clazz, annotation) != null;
	}

	/**
	 * check if the given Annotation class is present on this method
	 * @param method the method
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(MethodNode method, Class<? extends Annotation> annotation) {
		return getAnnotationRaw(method, annotation) != null;
	}

	/**
	 * Checks if the given {@link org.objectweb.asm.Type} represents a primitive or void
	 * @param type the type
	 * @return true if the {@code Type} represents a primitive type or void
	 */
	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.ARRAY && type.getSort() != Type.OBJECT && type.getSort() != Type.METHOD;
	}

	/**
	 * <p>Create a new {@link org.objectweb.asm.Type} that represents an array with {@code dimensions} dimensions and the
	 * Component Type {@code elementType}.</p>
	 * @param elementType the component type of the array type to create, must not be a Method type.
	 * @param dimensions the number of dimensions to create
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

	/**
	 * @deprecated use {@link de.take_weiland.mods.commons.asm.ClassInfo#isAssignableFrom(ClassInfo)}
	 */
	@Deprecated
	public static boolean isAssignableFrom(ClassInfo parent, ClassInfo child) {
		return parent.isAssignableFrom(child);
	}

}
