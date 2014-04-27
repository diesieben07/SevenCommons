package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author diesieben07
 */
public abstract class AbstractMatcher implements CodeMatcher {

	public final CodeLocation find(InsnList insns) {
		return findFirst(insns);
	}

	@Override
	public CodeLocation findFirst(InsnList target) {
		int sizeRemaining = target.size();
		checkSize(sizeRemaining);

		Iterator<AbstractInsnNode> it = target.iterator();
		while (it.hasNext()) {
			AbstractInsnNode insn = it.next();
			AbstractInsnNode endPoint = matchEndPoint(insn);
			if (endPoint != null) {
				return new CodeLocation(target, insn, endPoint);
			}
			// if remaining size is smaller than our size we can't match anymore
			checkSize(--sizeRemaining);
		}
		throw new NoMatchException();
	}

	@Override
	public CodeLocation findLast(InsnList target) {
		checkSize(target.size());
		// start the iterator with previous() pointing to the first possible match,
		// which is insns.size() elements from the end
		ListIterator<AbstractInsnNode> it = target.iterator(target.size() - size() + 1);
		while (it.hasPrevious()) {
			AbstractInsnNode insn = it.previous();
			AbstractInsnNode endPoint = matchEndPoint(insn);
			if (endPoint != null) {
				return new CodeLocation(target, insn, endPoint);
			}
		}
		throw new NoMatchException();
	}

	@Override
	public CodeLocation findOnly(InsnList target) {
		int sizeRemaining = target.size();
		checkSize(sizeRemaining);

		CodeLocation found = null;

		Iterator<AbstractInsnNode> it = target.iterator();
		while (it.hasNext()) {
			AbstractInsnNode insn = it.next();
			AbstractInsnNode endPoint = matchEndPoint(insn);
			if (endPoint != null) {
				if (found != null) {
					throw new IllegalArgumentException("More than one occurrence found!");
				}
				found = new CodeLocation(target, insn, endPoint);
			}
			// if remaining size is smaller than our size we can't match anymore
			checkSize(--sizeRemaining);
		}
		throw new NoMatchException();
	}

	@Override
	public List<CodeLocation> findAll(InsnList target) {
		if (target.size() < size()) {
			return ImmutableList.of();
		}

		ImmutableList.Builder<CodeLocation> b = ImmutableList.builder();
		Iterator<AbstractInsnNode> it = target.iterator();
		int sizeRemaining = target.size();
		while (true) {
			AbstractInsnNode insn = it.next();
			AbstractInsnNode endPoint = matchEndPoint(insn);
			if (endPoint != null) {
				b.add(new CodeLocation(target, insn, endPoint));
			}

			// if we have less elements left than our size we've found all matches
			if (--sizeRemaining < size()) {
				return b.build();
			}
		}
	}

	private void checkSize(int sizeRemaining) {
		if (sizeRemaining < size()) {
			throw new NoMatchException();
		}
	}

	protected abstract int size();

	protected abstract AbstractInsnNode matchEndPoint(AbstractInsnNode start);

}
