package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.INSTANCEOF;

/**
 * @author diesieben07
 */
class ChainedMatcher extends AbstractMatcher implements CodeMatcher.Chained {

	private final List<CodeMatcher> nodes = Lists.newArrayList();
	private int size = 0;
	private boolean allowSkipping = false;

	@Override
	public Chained andThen(CodeMatcher matcher) {
		nodes.add(checkNotNull(matcher, "matcher"));
		int oSize = matcher.size();
		if (size >= 0 && oSize > 0) {
			int newSize = size + oSize;
			if (newSize < 0) {
				size = -1;
			} else {
				size = newSize;
			}
		}
		return this;
	}

	@Override
	public Chained allowSkipping() {
		allowSkipping = true;
		return this;
	}

	@Override
	public Chained onFailure(FailureAction action) {
		super.onFailure(action);
		return this;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	protected AbstractInsnNode matchEndPoint(InsnList list, AbstractInsnNode start) {
		checkState(!nodes.isEmpty(), "Must at least add one matcher!");

		AbstractInsnNode lastMatchEnd = start;

		List<CodeMatcher> nodes = this.nodes;
		int len = nodes.size();
		System.out.println("starting matching at node idx " + list.indexOf(start) + ", opcode=" + start.getOpcode());
		if (start.getOpcode() == INSTANCEOF) {
			System.out.println("  == IS INSTANCEOF !! ==");
			System.out.println("     " + ((TypeInsnNode) start).desc);
		}
		for (int i = 0; true; ++i) {
			System.out.println("  matcher no. " + i);
			CodeMatcher matcher = nodes.get(i);
			FailureAction prevAction = matcher.getFailureAction();
			matcher.onFailure(FailureAction.RETURN_NULL);

			CodeLocation match = matcher.findFirstAfter(list, lastMatchEnd);
			if (match == null) {
				System.out.println("    " + matcher + " didn't match!");
				matcher.onFailure(prevAction);
				return null;
			} else {
				System.out.println("    " + matcher
						+ " matched at "
						+ match.first().getClass().getSimpleName()
						+ '[' + match.first().getOpcode() + ']'
						+ ", idx=" + list.indexOf(match.first()));
			}

			if (i != len - 1) {
				lastMatchEnd = match.last().getNext();
				if (lastMatchEnd == null) {
					matcher.onFailure(prevAction);
					return null;
				}
			} else {
				matcher.onFailure(prevAction);
				return match.last();
			}
		}
	}

}
