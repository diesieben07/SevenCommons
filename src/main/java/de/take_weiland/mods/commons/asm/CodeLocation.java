package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Iterators;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
public final class CodeLocation implements Iterable<AbstractInsnNode> {

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

	public AbstractInsnNode first() {
		return first;
	}

	public AbstractInsnNode last() {
		return last;
	}

	public InsnList list() {
		return list;
	}

	@Override
	public Iterator<AbstractInsnNode> iterator() {
		Iterator<AbstractInsnNode> it = list.iterator(list.indexOf(first));
		if (last == list.getLast()) {
			return it;
		} else {
			return Iterators.limit(it, list.size() - list.indexOf(last) + 1);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CodeLocation)) return false;

		CodeLocation that = (CodeLocation) o;
		return this.last.equals(that.last) && this.first.equals(that.first) && this.list.equals(that.list);
	}

	@Override
	public int hashCode() {
		int result = list.hashCode();
		result = 31 * result + first.hashCode();
		result = 31 * result + last.hashCode();
		return result;
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
}
