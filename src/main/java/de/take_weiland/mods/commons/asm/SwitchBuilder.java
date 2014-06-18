package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.GOTO;

/**
 * @author diesieben07
 */
public final class SwitchBuilder {

	private List<Entry> entries = Lists.newArrayList();
	private CodePiece dflt;
	private Set<Integer> keys = Sets.newHashSet();

	public SwitchBuilder add(int index, CodePiece code) {
		Integer box = index;
		checkState(!keys.contains(box), "Duplicate case statement");
		checkNotNull(code, "code");
		keys.add(box);
		entries.add(new Entry(index, code));
		return this;
	}

	public SwitchBuilder _default(CodePiece code) {
		checkState(dflt == null, "Cannot specify two default blocks");
		checkNotNull(code, "code");
		dflt = code;
		return this;
	}

	public CodePiece build(CodePiece value) {
		checkState(!entries.isEmpty(), "empty switch statement");
		checkState(dflt != null, "switch without default");

		List<Entry> ordered = Ordering.natural()
				.onResultOf(getIndexFunc())
				.immutableSortedCopy(entries);

		int[] keys = new int[ordered.size()];
		for (int i = 0, len = keys.length; i < len; ++i) {
			keys[i] = ordered.get(i).index;
		}

		LabelNode[] labels = new LabelNode[keys.length];
		for (int i = 0, len = labels.length; i < len; ++i) {
			labels[i] = new LabelNode();
		}
		LabelNode dfltLabel = new LabelNode();

		LabelNode afterSwitch = new LabelNode();

		CodeBuilder builder = new CodeBuilder();
		builder.add(value);
		builder.add(new LookupSwitchInsnNode(dfltLabel, keys, labels));

		for (int i = 0, len = labels.length; i < len; ++i) {
			builder.add(labels[i]);
			CodePiece code = ordered.get(i).code;

			builder.add(code);
			if (code.size() == 0 || !ASMUtils.isReturn(code.build().getLast().getOpcode())) {
				builder.add(new JumpInsnNode(GOTO, afterSwitch));
			}
		}

		builder.add(dfltLabel);
		builder.add(dflt);

		builder.add(afterSwitch);

		return builder.build();
	}

	private static Function<Entry, Integer> getIndexFunc() {
		return new Function<Entry, Integer>() {
			@Override
			public Integer apply(Entry input) {
				return input.index;
			}
		};
	}

	private static class Entry {

		final int index;
		final CodePiece code;

		private Entry(int index, CodePiece code) {
			this.index = index;
			this.code = code;
		}
	}

}
