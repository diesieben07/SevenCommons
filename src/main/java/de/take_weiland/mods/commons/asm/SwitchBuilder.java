package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static de.take_weiland.mods.commons.asm.CodePieces.constant;
import static org.objectweb.asm.Opcodes.GOTO;

/**
 * <p>A helper class for building switch statements in bytecode. This class will choose automatically between a tableswitch
 * and a lookupswitch.</p>
 * @author diesieben07
 */
public final class SwitchBuilder {

	private static final int LOOKUPSWITCH_THRESHOLD = 3;
	private final CodePiece value;
	private final Map<Integer, CodePiece> bodies = Maps.newHashMap();
	private final List<Integer> keysInOrder = Lists.newArrayList();
	private LabelNode breakLabel;
	private final ContextKey context = new ContextKey();

	/**
	 * <p>Create a new SwitchBuilder that performs a switch on the given value.</p>
	 * @param value the value to switch on
	 */
	public SwitchBuilder(CodePiece value) {
		this.value = checkNotNull(value);
	}

	/**
	 * <p>Add the given body for the given case statement.</p>
	 * @param key the key
	 * @param body the body
	 * @return this, for convenience
	 */
	public SwitchBuilder add(int key, CodePiece body) {
		Integer keyBox = key;
		if (bodies.put(keyBox, checkNotNull(body)) != null) {
			throw new IllegalArgumentException("Duplicate key " + keyBox);
		}
		keysInOrder.add(keyBox);
		return this;
	}

	/**
	 * <p>Add the given code as the default body for the switch.</p>
	 * @param defaultBody the code
	 * @return this, for convenience
	 */
	public SwitchBuilder onDefault(CodePiece defaultBody) {
		checkState(!bodies.containsKey(null), "Can only specify one default body!");
		keysInOrder.add(null);
		bodies.put(null, checkNotNull(defaultBody));
		return this;
	}

	private BreakPlaceholder _break;

	/**
	 * <p>Get a CodePiece that represents a {@code break} statement within this switch statement.</p>
	 * @return a CodePiece
	 */
	public CodePiece getBreak() {
		return _break == null ? _break = new BreakPlaceholder() :_break;
	}

	/**
	 * <p>Build this SwitchBuilder into a CodePiece.</p>
	 * <p>This method will make optimizations, such as turning a switch statement with a single body into an equivalent
	 * if-statement.</p>
	 * @return a CodePiece
	 */
	public CodePiece build() {
		int size = bodies.size();
		boolean hasDefault = bodies.containsKey(null);
		if (size == 0) {
			return CodePieces.of();
		} else if (size == 1) { // we only have one branch, so an if statement
			CodePiece onlyBody = Iterables.getOnlyElement(bodies.values());
			Integer onlyKey = Iterables.getOnlyElement(bodies.keySet());
			if (onlyKey == null) { // only got a default branch
				return onlyBody;
			} else {
				return ASMCondition.ifSame(constant((int) onlyKey), value, Type.INT_TYPE).doIfTrue(onlyBody);
			}
		} else if (size == 2 && hasDefault) { // we only have one key + default
			CodePiece defaultBody = bodies.remove(null);
			CodePiece onlyKey = constant((int) Iterables.getOnlyElement(bodies.keySet()));
			CodePiece onlyBody = Iterables.getOnlyElement(bodies.values());

			return ASMCondition.ifEqual(onlyKey, value, Type.INT_TYPE).doIfElse(onlyBody, defaultBody);
		} else {
			if (!hasDefault) {
				bodies.put(null, CodePieces.of());
				keysInOrder.add(null);
			}
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
