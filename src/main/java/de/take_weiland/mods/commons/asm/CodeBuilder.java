package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public final class CodeBuilder {

	private boolean hasNonCodePiece = false;
	private ArrayList<Object> objects = Lists.newArrayList();

	public CodeBuilder add(@NotNull AbstractInsnNode insn) {
		checkNotBuilt();
		objects.add(checkNotNull(insn));
		hasNonCodePiece = true;
		return this;
	}

	public CodeBuilder add(@NotNull InsnList list) {
		checkNotBuilt();
		objects.add(checkNotNull(list));
		hasNonCodePiece = true;
		return this;
	}

	public CodeBuilder add(@NotNull CodePiece piece) {
		checkNotBuilt();
		objects.add(checkNotNull(piece));
		return this;
	}

	private void checkNotBuilt() {
		checkState(objects != null, "Already built");
	}

	public CodePiece build() {
		ArrayList<Object> list = objects;
		objects = null;

		list.trimToSize();
		if (hasNonCodePiece) {
			return new MixedCombinedCodePiece(list);
		} else {
			// this cast is safe because:
			// a) CombinedCodePiece never puts anything in the list
			// b) we never write to this list after this point
			// c) this list only contains CodePieces
			//noinspection unchecked,rawtypes
			return new CombinedCodePiece((List) list);
		}
	}

}
