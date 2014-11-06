package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Simple helper for finding hooks in bytecode, usually to insert hooks.</p>
 * <p>It is based on Stages, where each stage starts where the last one left off and finds the next hook. The result of the
 * CodeSearcher is the result of the last stage.</p>
 *
 * @author diesieben07
 */
public final class CodeSearcher {

	private final List<Stage> stages = Lists.newArrayList();

	/**
	 * <p>Add a Stage of finding the hook.</p>
	 * @param stage the stage
	 * @return this, for convenience
	 */
	public CodeSearcher add(Stage stage) {
		stages.add(checkNotNull(stage));
		return this;
	}

	/**
	 * <p>Find the result of this CodeSearcher in the given InsnList.</p>
	 * @param list the InsnList
	 * @return the resulting instruction
	 */
	public AbstractInsnNode find(InsnList list) {
		return find(list.getFirst());
	}

	/**
	 * <p>Find the result of this CodeSearcher when starting at the given instruction. The instruction must be part of an
	 * InsnList.</p>
	 * @param start the starting point
	 * @return the resulting instruction
	 */
	public AbstractInsnNode find(AbstractInsnNode start) {
		AbstractInsnNode current = start;
		for (Stage stage : stages) {
			current = stage.findNext(current);
		}
		return current;
	}

	/**
	 * <p>A stage in the CodeSearcher.</p>
	 */
	public static interface Stage {

		/**
		 * <p>Find the next instruction. It will be passed to the next stage or, if this is the last stage, describes the result
		 * of the CodeSearcher.</p>
		 * @param current the current instruction
		 * @return the next instruction
		 */
		AbstractInsnNode findNext(AbstractInsnNode current);

	}

}
