package de.take_weiland.mods.commons.asm;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class SwitchBuilder {

	private static final int LOOKUPSWITCH_THRESHOLD = 6;
	private final CodePiece value;
	private final Map<Integer, CodePiece> bodies = Maps.newHashMap();
	private final List<Integer> keysInOrder = Lists.newArrayList();
	private LabelNode breakLabel;
	private final ContextKey context = ContextKey.create();

	public SwitchBuilder(CodePiece value) {
		this.value = checkNotNull(value);
	}

	public SwitchBuilder add(int key, CodePiece body) {
		Integer keyBox = key;
		if (bodies.put(keyBox, checkNotNull(body)) != null) {
			throw new IllegalArgumentException("Duplicate key " + keyBox);
		}
		keysInOrder.add(keyBox);
		return this;
	}

	public SwitchBuilder onDefault(CodePiece defaultBody) {
		checkState(!bodies.containsKey(null), "Can only specify one default body!");
		keysInOrder.add(null);
		bodies.put(null, checkNotNull(defaultBody));
		return this;
	}

	private BreakPlaceholder _break;

	public CodePlaceholder getBreak() {
		return _break == null ? _break = new BreakPlaceholder() :_break;
	}

	public CodePiece build() {
		checkState(bodies.containsKey(null), "switch without default");
		int size = bodies.size();
		if (size == 1) {
			return CodePieces.concat(value, CodePieces.ofOpcode(POP), bodies.get(null));
		} else if (size == 2) {
			CodePiece defaultBody = bodies.remove(null);
			return CodePieces.doIfElse(IF_ICMPEQ,
					CodePieces.constant(Iterables.getOnlyElement(bodies.keySet()).intValue()).append(value),
					Iterables.getOnlyElement(bodies.values()), defaultBody);
		} else {
			CodeBuilder builder = new CodeBuilder();

			int[] keys = new int[size];
			Iterator<Integer> it = bodies.keySet().iterator();
			int i = 0;
			while (it.hasNext()) {
				Integer key = it.next();
				if (key == null) {
					continue;
				}
				keys[i++] = key;
			}
			Arrays.sort(keys);

			size--;

			int maxDiff = 0;
			for (i = size - 1; i > 0; i--) {
				maxDiff = Math.max(maxDiff, Math.abs(keys[i] - keys[i - 1]));
			}

			final LabelNode after = breakLabel = new LabelNode();
			Map<Integer, LabelNode> keyToLabel = Maps.newHashMapWithExpectedSize(size);

			for (Integer key : keysInOrder) {
				keyToLabel.put(key, new LabelNode());
			}
			LabelNode dflt = keyToLabel.get(null);

			builder.add(value);

			if (maxDiff >= LOOKUPSWITCH_THRESHOLD) {
				LabelNode[] labels = new LabelNode[size];
				for (i = 0; i < size; i++) {
					labels[i] = keyToLabel.get(keys[i]);
				}
				builder.add(new LookupSwitchInsnNode(dflt, keys, labels), context);
				for (Integer key : keysInOrder) {
					builder.add(keyToLabel.get(key), context);
					builder.add(bodies.get(key));
				}
				builder.add(after, context);
			} else {
				int min = keys[0];
				int max = keys[keys.length - 1];
				int numLbls = max - min + 1;
				LabelNode[] labels = new LabelNode[numLbls];
				for (i = 0; i < numLbls; i++) {
					LabelNode label = keyToLabel.get(i + min);
					labels[i] = label == null ? dflt : label;
				}
				builder.add(new TableSwitchInsnNode(min, max, dflt, labels), context);
				for (Integer key : keysInOrder) {
					builder.add(keyToLabel.get(key), context);
					builder.add(bodies.get(key));
				}
				builder.add(after, context);
			}

			return builder.build();
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("bodies", bodies).add("keys", keysInOrder).toString();
	}

	CodePiece breakPiece;

	private class BreakPlaceholder extends CodePlaceholder {

		@Override
		protected CodePiece resolve() {
			checkState(breakLabel != null);
			return breakPiece == null
					? (breakPiece = CodePieces.of(new JumpInsnNode(GOTO, breakLabel)).setContextKey(context))
					: breakPiece;
		}
	}

}
