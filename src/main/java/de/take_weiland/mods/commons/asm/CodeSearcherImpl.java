package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
class CodeSearcherImpl implements CodeSearcher {

	private final InsnList insns;
	private AbstractInsnNode pos;
	private boolean backwards = false;

	private AbstractInsnNode start;
	private AbstractInsnNode end;

	CodeSearcherImpl(InsnList insns) {
		checkArgument(insns.size() > 0, "list must not be empty!");
		this.insns = insns;
		this.pos = insns.getFirst();
	}

	@Override
	public CodeSearcher find(Predicate<? super AbstractInsnNode> predicate) {
		boolean backwards = this.backwards;
		AbstractInsnNode pos = resolvePos();

		do {
			pos = backwards ? pos.getPrevious() : pos.getNext();
		} while (pos != null && !predicate.apply(pos));

		this.pos = pos;

		if (pos == null) {
			doFail();
		}
		return this;
	}

	@Override
	public CodeSearcher find(int opcode) {
		return find(opcodeFinder(opcode));
	}

	@Override
	public CodeSearcher find(AbstractInsnNode insn, boolean lenient) {
		return find(matcher(insn, lenient));
	}

	@Override
	public <T extends AbstractInsnNode, S> CodeSearcher find(Class<T> type, Function<? super T, ? extends S> function, Predicate<? super S> predicate) {
		return find(type, Predicates.compose(predicate, function));
	}

	@Override
	public <S> CodeSearcher find(Function<? super AbstractInsnNode, ? extends S> function, Predicate<? super S> predicate) {
		return find(Predicates.compose(predicate, function));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractInsnNode> CodeSearcher find(final Class<T> type, final Predicate<? super T> predicate) {
		// cast is safe, because and() short-circuits if it's no T
		return find(Predicates.and(Predicates.instanceOf(type), (Predicate<AbstractInsnNode>)predicate));
	}

	@Override
	public CodeSearcher find(AbstractInsnNode insn) {
		return find(insn, true);
	}

	@Override
	public CodeSearcher jumpToStart() {
		pos = backwards ? insns.getFirst().getNext() : null;
		return this;
	}

	@Override
	public CodeSearcher jumpToEnd() {
		pos = backwards ? null : insns.getLast().getPrevious();
		return this;
	}

	@Override
	public CodeSearcher backwards() {
		backwards = true;
		return this;
	}

	@Override
	public CodeSearcher forwards() {
		backwards = false;
		return this;
	}

	@Override
	public CodeSearcher startHere() {
		start = resolvePos();
		return this;
	}

	@Override
	public CodeLocation endHere() {
		checkState(start != null, "Start not set!");
		return CodeLocation.create(insns, start, resolvePos());
	}

	@Override
	public CodeSearcher reset() {
		pos = start = null;
		backwards = false;
		return this;
	}

	private void doFail() {
		throw new NoSuchElementException();
	}

	private static Predicate<AbstractInsnNode> opcodeFinder(final int opcode) {
		return new Predicate<AbstractInsnNode>() {
			@Override
			public boolean apply(AbstractInsnNode input) {
				return input.getOpcode() == opcode;
			}
		};
	}

	private AbstractInsnNode resolvePos() {
		AbstractInsnNode pos = this.pos;
		return pos == null ? (backwards ? insns.getLast() : insns.getFirst()) : pos;
	}

	private static Predicate<AbstractInsnNode> matcher(final AbstractInsnNode insn, final boolean lenient) {
		return new Predicate<AbstractInsnNode>() {
			@Override
			public boolean apply(AbstractInsnNode input) {
				return ASMUtils.matches(input, insn, lenient);
			}
		};
	}
}
