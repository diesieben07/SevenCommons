package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
public abstract class AbstractMatcher implements CodeMatcher {

	private FailureAction onFail = FailureAction.THROW;

	@Override
	public CodeLocation findFirstBetween(InsnList target, AbstractInsnNode first, AbstractInsnNode last) {
		int firstIdx = ASMUtils.fastIdx(target, first);
		int lastIdx = ASMUtils.fastIdx(target, last);
		checkLastFirst(firstIdx, lastIdx);
		System.out.println("first=" + firstIdx + ", last=" + lastIdx);

		int mySize = size();
		System.out.println("mySize=" + mySize);
		int sizeRemaining = lastIdx - firstIdx + 1;
		if (mySize >= 0 && sizeRemaining < mySize) {
			System.out.println("list too small from teh beginning!");
			return noMatch();
		}

		AbstractInsnNode fence = last.getNext();
		Iterator<AbstractInsnNode> it = target.iterator(firstIdx);
		while (it.hasNext()) {
			AbstractInsnNode insn = it.next();
			if (insn == fence) {
				break;
			}
//			System.out.println("    trying at " + target.indexOf(insn));
			AbstractInsnNode endPoint = matchEndPoint(target, insn);
			if (endPoint != null) {
				return CodeLocation.create(target, insn, endPoint);
			}
			// if remaining size is smaller than our size we can't match anymore
			if (mySize >= 0 && --sizeRemaining < mySize) {
				System.out.println("ran out of elements!");
				return noMatch();
			}
		}
		return noMatch();
	}

	@Override
	public CodeLocation findLastBetween(InsnList target, AbstractInsnNode first, AbstractInsnNode last) {
		int firstIdx = ASMUtils.fastIdx(target, first);
		int lastIdx = ASMUtils.fastIdx(target, last);
		checkLastFirst(firstIdx, lastIdx);

		int mySize = size();
		int sizeRemaining = lastIdx - firstIdx + 1;
		if (mySize >= 0 && sizeRemaining < mySize) {
			return noMatch();
		}

		final AbstractInsnNode fence = first.getPrevious();

		// index of the last possible match,
		// which is the lastIndex possible or mySize elements from the end of the list,
		// whichever is smaller
		int lastPossibleMatch = Math.min(lastIdx, target.size() - mySize);
		// start the iterator with previous() pointing to the first possible match
		ListIterator<AbstractInsnNode> it = target.iterator(lastPossibleMatch + 1);
		while (it.hasPrevious()) {
			AbstractInsnNode insn = it.previous();
			// if we've gone past the fence, bail out
			if (insn == fence) {
				break;
			}
			AbstractInsnNode endPoint = matchEndPoint(target, insn);
			if (endPoint != null) {
				return CodeLocation.create(target, insn, endPoint);
			}
		}
		return noMatch();
	}

	@Override
	public CodeLocation findOnlyBetween(InsnList target, AbstractInsnNode first, AbstractInsnNode last) {
		int firstIdx = ASMUtils.fastIdx(target, first);
		int lastIdx = ASMUtils.fastIdx(target, last);
		checkLastFirst(firstIdx, lastIdx);

		int mySize = size();
		int sizeRemaining = lastIdx - firstIdx + 1;
		if (mySize >= 0 && sizeRemaining < mySize) {
			return noMatch();
		}

		CodeLocation found = null;

		AbstractInsnNode fence = last.getNext();
		Iterator<AbstractInsnNode> it = target.iterator(firstIdx);
		while (it.hasNext()) {
			AbstractInsnNode insn = it.next();
			if (insn == fence) {
				break;
			}
			AbstractInsnNode endPoint = matchEndPoint(target, insn);
			if (endPoint != null) {
				if (found != null) {
					throw new IllegalArgumentException("More than one occurrence found!");
				}
				found = CodeLocation.create(target, insn, endPoint);
			}
			// if remaining size is smaller than our size we can't match anymore
			if (mySize >= 0 && --sizeRemaining < mySize) {
				break;
			}
		}
		return found == null ? noMatch() : found;
	}

	@Override
	public List<CodeLocation> findAllBetween(InsnList target, AbstractInsnNode first, AbstractInsnNode last) {
		int firstIdx = ASMUtils.fastIdx(target, first);
		int lastIdx = ASMUtils.fastIdx(target, last);
		checkLastFirst(firstIdx, lastIdx);
		int sizeRemaining = lastIdx - firstIdx + 1;
		if (sizeRemaining < size()) {
			return ImmutableList.of();
		}

		int mySize = size();
		AbstractInsnNode fence = last.getNext();
		ImmutableList.Builder<CodeLocation> b = ImmutableList.builder();
		Iterator<AbstractInsnNode> it = target.iterator(firstIdx);
		while (true) {
			AbstractInsnNode insn = it.next();
			// if we've passed the fence, bail out
			if (insn == fence) {
				break;
			}
			AbstractInsnNode endPoint = matchEndPoint(target, insn);
			if (endPoint != null) {
				b.add(CodeLocation.create(target, insn, endPoint));
			}

			// if we have less elements left than our size we've found all matches
			if (mySize >= 0 && --sizeRemaining < mySize) {
				break;
			}
		}
		return b.build();
	}

	@Override
	public Chained andThen(CodeMatcher next) {
		return new ChainedMatcher().andThen(this).andThen(next);
	}

	/**
	 * <p>Find the next end point of a match in the given list, starting at the given node
	 * or null if no match found</p>
	 * @param list the InsnList
	 * @param start the starting point to start searching from
	 * @return the next match or null if no match
	 */
	protected abstract AbstractInsnNode matchEndPoint(InsnList list, AbstractInsnNode start);

	// private helper methods

	private void checkSize(int sizeRemaining) {
		if (sizeRemaining < size()) {
			throw new NoMatchException();
		}
	}

	private static void checkLastFirst(int firstIdx, int lastIdx) {
		checkArgument(firstIdx <= lastIdx, "last comes before first");
	}

	private CodeLocation noMatch() {
		if (onFail == FailureAction.THROW) {
			throw new NoMatchException();
		}
		return null;
	}

	// the following methods all redirect to the find***Between versions
	// and should rarely be needed to be overridden
	@Override
	public CodeLocation findFirst(InsnList list) {
		return list.size() == 0 ? noMatch() : findFirstBetween(list, list.getFirst(), list.getLast());
	}

	@Override
	public CodeLocation findLast(InsnList list) {
		return list.size() == 0 ? noMatch() : findLastBetween(list, list.getFirst(), list.getLast());
	}

	@Override
	public CodeLocation findOnly(InsnList list) {
		return list.size() == 0 ? noMatch() : findOnlyBetween(list, list.getFirst(), list.getLast());
	}

	@Override
	public List<CodeLocation> findAll(InsnList list) {
		if (list.size() == 0) {
			return ImmutableList.of();
		}
		return findAllBetween(list, list.getFirst(), list.getLast());
	}

	@Override
	public CodeLocation findOnlyBefore(InsnList list, AbstractInsnNode last) {
		return list.size() == 0 ? noMatch() : findOnlyBetween(list, list.getFirst(), last);
	}

	@Override
	public CodeLocation findFirstBefore(InsnList list, AbstractInsnNode last) {
		return list.size() == 0 ? noMatch() : findFirstBetween(list, list.getFirst(), last);
	}

	@Override
	public CodeLocation findLastBefore(InsnList list, AbstractInsnNode last) {
		return list.size() == 0 ? noMatch() : findLastBetween(list, list.getFirst(), last);
	}

	@Override
	public List<CodeLocation> findAllBefore(InsnList list, AbstractInsnNode last) {
		if (list.size() == 0) {
			return ImmutableList.of();
		}
		return findAllBetween(list, list.getFirst(), last);
	}

	@Override
	public CodeLocation findOnlyAfter(InsnList list, AbstractInsnNode first) {
		return list.size() == 0 ? noMatch() : findOnlyBetween(list, first, list.getLast());
	}

	@Override
	public CodeLocation findFirstAfter(InsnList list, AbstractInsnNode first) {
		return list.size() == 0 ? noMatch() : findFirstBetween(list, first, list.getLast());
	}

	@Override
	public CodeLocation findLastAfter(InsnList list, AbstractInsnNode first) {
		return list.size() == 0 ? noMatch() : findLastBetween(list, first, list.getLast());
	}

	@Override
	public List<CodeLocation> findAllAfter(InsnList list, AbstractInsnNode first) {
		if (list.size() == 0) {
			return ImmutableList.of();
		}
		return findAllBetween(list, first, list.getLast());
	}

	@Override
	public boolean throwsExceptions() {
		return onFail == FailureAction.THROW;
	}

	@Override
	public FailureAction getFailureAction() {
		return onFail;
	}

	@Override
	public CodeMatcher onFailure(FailureAction action) {
		onFail = checkNotNull(action);
		return this;
	}

}
