package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
public final class CodeSearcher {

	private final List<Stage> stages = Lists.newArrayList();

	public CodeSearcher add(Stage stage) {
		stages.add(checkNotNull(stage));
		return this;
	}

	public AbstractInsnNode find(InsnList list) {
		return find(list.getFirst());
	}

	public AbstractInsnNode find(AbstractInsnNode start) {
		AbstractInsnNode current = start;
		for (Stage stage : stages) {
			current = stage.findNext(current);
		}
		return current;
	}

	public static interface Stage {

		AbstractInsnNode findNext(AbstractInsnNode current);

	}

}
