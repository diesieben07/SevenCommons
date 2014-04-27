package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
public final class CodeLocation implements Cloneable {

	private final InsnList list;
	private AbstractInsnNode first;
	private AbstractInsnNode last;

	public static CodeLocation create(InsnList list, AbstractInsnNode first, AbstractInsnNode last) {
		return new CodeLocation(list, first, last);
	}

	public static CodeLocation all(InsnList list) {
		return new CodeLocation(list, list.getFirst(), list.getLast());
	}

	public static CodeLocation allFrom(InsnList list, AbstractInsnNode first) {
		return new CodeLocation(list, first, list.getLast());
	}

	public static CodeLocation allTo(InsnList list, AbstractInsnNode last) {
		return new CodeLocation(list, list.getFirst(), last);
	}

	public static CodeLocation allFromExcl(InsnList list, AbstractInsnNode beforeFirst) {
		return allFrom(list, requireNext(beforeFirst));
	}

	public static CodeLocation allToExcl(InsnList list, AbstractInsnNode afterLast) {
		return allTo(list, requirePrev(afterLast));
	}

	private static AbstractInsnNode requireNext(AbstractInsnNode insn) {
		AbstractInsnNode next = insn.getNext();
		checkArgument(next != null, "instruction must have next");
		return next;
	}

	private static AbstractInsnNode requirePrev(AbstractInsnNode insn) {
		AbstractInsnNode next = insn.getPrevious();
		checkArgument(next != null, "instruction must have previous");
		return next;
	}

	private CodeLocation(InsnList list, AbstractInsnNode first, AbstractInsnNode last) {
		this.list = list;
		this.first = first;
		this.last = last;
	}

	public AbstractInsnNode first() {
		return first;
	}

	public AbstractInsnNode last() {
		return last;
	}

	public InsnList list() {
		return list;
	}

	public CodeLocation moveForward(int steps) {
		// Do end first, it is more likely to fail
		last = ASMUtils.getNext(last, steps);
		first = ASMUtils.getNext(first, steps);
		return this;
	}

	public CodeLocation moveBackward(int steps) {
		first = ASMUtils.getPrevious(first, steps);
		last = ASMUtils.getPrevious(last, steps);
		return this;
	}

	// we're final, this is safe
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public CodeLocation clone() {
		return new CodeLocation(list, first, last);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CodeLocation)) return false;

		CodeLocation that = (CodeLocation) o;

		return last.equals(that.last) && first.equals(that.first) && list.equals(that.list);
	}

	@Override
	public int hashCode() {
		int result = list.hashCode();
		result = 31 * result + first.hashCode();
		result = 31 * result + last.hashCode();
		return result;
	}
}
