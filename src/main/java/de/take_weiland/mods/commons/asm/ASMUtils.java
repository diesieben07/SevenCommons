package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.launchwrapper.IClassNameTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
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
		AbstractInsnNode node = findLast(method, Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN));
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
		return findMethod(clazz, useMcpNames() ? mcpName : srgName);
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
		return findMethod(clazz, useMcpNames() ? mcpName : srgName, desc);
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

	/**
	 * <p>Finds all Fields and Methods in the given class which have the given Annotation
	 * and transforms them into {@link de.take_weiland.mods.commons.asm.FieldAccess FieldAccesses}</p>
	 * @param clazz the ClassNode
	 * @param annotation
	 * @return
	 */
	public static Collection<FieldAccess> fieldsOrGettersWith(final ClassNode clazz, Class<? extends Annotation> annotation) {
		return ImmutableList.copyOf(Iterators.concat(
			Iterators.transform(
					methodsWith(clazz, annotation).iterator(),
					asFieldAccessFuncM(clazz)),
			Iterators.transform(
					fieldsWith(clazz, annotation).iterator(),
					asFieldAccessFuncF(clazz))
		));
	}

	private static Function<FieldNode, FieldAccess> asFieldAccessFuncF(final ClassNode clazz) {
		return new Function<FieldNode, FieldAccess>() {
			@Override
			public FieldAccess apply(FieldNode field) {
				return asFieldAccess(clazz, field);
			}
		};
	}

	private static Function<MethodNode, FieldAccess> asFieldAccessFuncM(final ClassNode clazz) {
		return new Function<MethodNode, FieldAccess>() {
			@Override
			public FieldAccess apply(MethodNode method) {
				return asFieldAccess(clazz, method);
			}
		};
	}

	/**
	 * Creates a {@link de.take_weiland.mods.commons.asm.FieldAccess} that represents the given FieldNode.
	 * @param clazz the ClassNode
	 * @param field the FieldNode
	 * @return a FieldAccess
	 */
	public static FieldAccess asFieldAccess(ClassNode clazz, FieldNode field) {
		return new FieldAccessDirect(clazz, field);
	}

	/**
	 * Creates a {@link de.take_weiland.mods.commons.asm.FieldAccess} that represents the given getter method.
	 * @param clazz the ClassNode
	 * @param getter the MethodNode representing the getter
	 * @return a FieldAccess
	 */
	public static FieldAccess asFieldAccess(ClassNode clazz, MethodNode getter) {
		return new FieldAccessWrapped(clazz, getter, null);
	}

	/**
	 * Creates a {@link de.take_weiland.mods.commons.asm.FieldAccess} that represents the given getter and setter method.
	 * @param clazz the ClassNode
	 * @param getter the MethodNode representing the getter
	 * @param setter the MethodNode representing the setter
	 * @return a FieldAccess
	 */
	public static FieldAccess asFieldAccess(ClassNode clazz, MethodNode getter, MethodNode setter) {
		return new FieldAccessWrapped(clazz, getter, setter);
	}

	/**
	 * <p>Converts the given InsnList to a {@link de.take_weiland.mods.commons.asm.CodePiece}</p>
	 * <p>The InsnList must not be modified externally after it was converted, if you need to do so,
	 * {@link #clone(org.objectweb.asm.tree.InsnList)} first.</p>
	 * <p>The InsnList must not be newly created, if it is not, call {@link #clone(org.objectweb.asm.tree.InsnList)}
	 * first, and pass the result to this method.</p>
	 * @param insns the InsnList
	 * @return a CodePiece representing all instructions in the InsnList
	 */
	public static CodePiece asCodePiece(InsnList insns) {
		switch (insns.size()) {
			case 0:
				return EmptyCodePiece.INSTANCE;
			case 1:
				return asCodePiece(insns.getFirst());
			default:
				return new InsnListCodePiece(insns);
		}
	}

	/**
	 * <p>Converts the given instruction to a {@link de.take_weiland.mods.commons.asm.CodePiece}</p>
	 * <p>The AbstractInsnNode must not be modified externally after it was converted, if you need to do so,
	 * {@link #clone(org.objectweb.asm.tree.AbstractInsnNode)} first.</p>
	 * <p>The AbstractInsnNode must not be part of an InsnList that is in use. If it is, call
	 * {@link #clone(org.objectweb.asm.tree.AbstractInsnNode)} first and pass the result to this method.</p>
	 * @param insn the instruction to represent as a CodePiece
	 * @return a CodePiece representing the single instruction
	 */
	public static CodePiece asCodePiece(AbstractInsnNode insn) {
		return new SingleInsnCodePiece(insn);
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

	private static final Function<MethodInsnNode, String> GET_METHOD_NAME = new Function<MethodInsnNode, String>() {
		@Override
		public String apply(MethodInsnNode input) {
			return input.name;
		}
	};

	public static Function<MethodInsnNode, String> methodInsnName() {
		return GET_METHOD_NAME;
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
			int idx;
			checkArgument((idx = SCASMAccessHook.getIndex(insn)) >= 0, "instruction doesn't belong to list!");
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
			int idx;
			checkArgument((idx = SCASMAccessHook.getIndex(insn)) >= 0, "instruction doesn't belong to list!");
			return list.get(idx - n);
		} else {
			return getPrevious(insn, n);
		}
	}

	public static CodeSearcher searchIn(InsnList insns) {
		return new CodeSearcherImpl(insns);
	}

	public static ListIterator<AbstractInsnNode> fastIterator(InsnList list) {
		if (list.size() == 0) {
			return ImmutableList.<AbstractInsnNode>of().listIterator();
		}
		return fastIterator(list, list.getFirst());
	}

	public static ListIterator<AbstractInsnNode> fastIterator(InsnList list, AbstractInsnNode start) {
		return new FastInsnListItr(checkNotNull(list, "list"), checkNotNull(start, "start"));
	}

	private static class FastInsnListItr implements ListIterator<AbstractInsnNode> {

		private final InsnList list;
		private AbstractInsnNode next;
		private AbstractInsnNode previous;
		private AbstractInsnNode lastReturned;

		private FastInsnListItr(InsnList list, AbstractInsnNode next) {
			this.list = list;
			this.next = next;
			this.previous = next.getPrevious();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public AbstractInsnNode next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			lastReturned = previous = next;
			next = next.getNext();
//			System.out.println("FastItr returning " + list.indexOf(lastReturned));
			return lastReturned;
		}

		@Override
		public boolean hasPrevious() {
			return previous != null;
		}

		@Override
		public AbstractInsnNode previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			lastReturned = next = previous;
			previous = previous.getPrevious();
			return lastReturned;
		}

		@Override
		public void remove() {
			checkState(lastReturned != null);
			list.remove(lastReturned);
			lastReturned = null;
		}

		@Override
		public void set(AbstractInsnNode insn) {
			checkState(lastReturned != null);
			list.set(lastReturned, insn);
			lastReturned = insn;
		}

		@Override
		public void add(AbstractInsnNode insn) {
			// if we have no next we are either at the end of the list
			// or the list is empty
			if (!hasNext()) {
				list.add(insn);
			} else {
				list.insertBefore(next, insn);
			}
			previous = insn;
			lastReturned = null;
		}

		@Override
		public int nextIndex() {
			return hasNext() ? fastIdx(list, next) : list.size();
		}

		@Override
		public int previousIndex() {
			return hasPrevious() ? fastIdx(list, previous) : -1;
		}
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

		AbstractInsnNode current = from;
		do {
			if (current == to) {
				break;
			}
			clone.add(current.clone(labels));
			current = current.getNext();
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
	 * Determine whether this is an obfuscated environment or not. True if MCP names should be used (development environment)
	 * @return true if this is a development environment and MCP names should be used.
	 */
	public static boolean useMcpNames() {
		return SevenCommons.MCP_ENVIRONMENT;
	}

	// *** Class name Utilities *** //

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

	private static ClassNode getClassNode0(String name, int readerFlags) {
		try {
			byte[] bytes = CLASSLOADER.getClassBytes(transformName(name));
			if (bytes == null) {
				return null;
			}
			return getClassNode(bytes, readerFlags);
		} catch (IOException e) {
			return null;
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
	public static AnnotationNode getAnnotation(FieldNode field, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(field.visibleAnnotations, field.invisibleAnnotations), ann);
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given class
	 * @param clazz the class
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(ClassNode clazz, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(clazz.visibleAnnotations, clazz.invisibleAnnotations), ann);
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given method
	 * @param method the method
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(MethodNode method, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(method.visibleAnnotations, method.invisibleAnnotations), ann);
	}

	private static AnnotationNode getAnnotation(Iterable<AnnotationNode> annotations, Class<? extends Annotation> ann) {
		String desc = Type.getDescriptor(ann);
		for (AnnotationNode node : annotations) {
			if (node.desc.equals(desc)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * check if the given Annotation class is present on this field
	 * @param field the field
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(FieldNode field, Class<? extends Annotation> annotation) {
		return getAnnotation(field, annotation) != null;
	}

	/**
	 * check if the given Annotation class is present on this class
	 * @param clazz the class
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		return getAnnotation(clazz, annotation) != null;
	}

	/**
	 * check if the given Annotation class is present on this method
	 * @param method the method
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(MethodNode method, Class<? extends Annotation> annotation) {
		return getAnnotation(method, annotation) != null;
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
	 * create a {@link ClassInfo} representing the given Class
	 * @param clazz the Class
	 * @return a ClassInfo
	 */
	public static ClassInfo getClassInfo(Class<?> clazz) {
		return new ClassInfoFromClazz(clazz);
	}

	/**
	 * create a {@link ClassInfo} representing the given ClassNode
	 * @param clazz the ClassNode
	 * @return a ClassInfo
	 */
	public static ClassInfo getClassInfo(ClassNode clazz) {
		return new ClassInfoFromNode(clazz);
	}

	/**
	 * <p>create a {@link ClassInfo} representing the given class.</p>
	 * <p>This method will not load any classes through the ClassLoader directly, but instead use the ASM library to analyze the raw class bytes.</p>
	 * @param className the class
	 * @return a ClassInfo
	 * @throws MissingClassException if the class could not be found
	 */
	public static ClassInfo getClassInfo(String className) {
		className = binaryName(className);
		Class<?> clazz;
		// first, try to get the class if it's already loaded
		if ((clazz = SevenCommons.REFLECTOR.findLoadedClass(CLASSLOADER, className)) != null) {
			return new ClassInfoFromClazz(clazz);
		// didn't find it. Try with the transformed name now
		} else if ((clazz = SevenCommons.REFLECTOR.findLoadedClass(CLASSLOADER, transformName(className))) != null) {
			return new ClassInfoFromClazz(clazz);
		} else {
			try {
				// the class is definitely not loaded, get it's bytes
				byte[] bytes = SevenCommons.CLASSLOADER.getClassBytes(transformName(className));
				// somehow we can't access the class bytes.
				// we try and load the class now
				if (bytes == null) {
					return tryLoad(className);
				} else {
					// we found the bytes, lets use them
					return new ClassInfoFromNode(getThinClassNode(bytes));
				}
			} catch (IOException e) {
				// something went wrong getting the class bytes. try and load it
				return tryLoad(className);
			}
		}
	}

	private static ClassInfo tryLoad(String className) {
		try {
			return getClassInfo(Class.forName(className));
		} catch (Exception e) {
			// we've tried everything. This class doesn't fucking exist.
			throw new MissingClassException(className, e);
		}
	}

	/**
	 * @deprecated use {@link de.take_weiland.mods.commons.asm.ClassInfo#isAssignableFrom(ClassInfo)}
	 */
	@Deprecated
	public static boolean isAssignableFrom(ClassInfo parent, ClassInfo child) {
		return parent.isAssignableFrom(child);
	}

}
