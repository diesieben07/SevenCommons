package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Iterables;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author diesieben07
 */
class CombinedCodePiece extends AbstractCodePiece {

	final Iterable<CodePiece> pieces;
	private int sizeCache = -1;

	CombinedCodePiece(CodePiece... pieces) {
		this(Arrays.asList(pieces));
	}

	CombinedCodePiece(Iterable<CodePiece> pieces) {
		this.pieces = pieces;
	}

	@Override
	public void insertBefore(InsnList list, AbstractInsnNode location) {
		for (CodePiece piece : pieces) {
			piece.insertBefore(list, location);
		}
	}

	@Override
	public void insertAfter(InsnList list, AbstractInsnNode location) {
		if (location == list.getLast()) {
			for (CodePiece piece : pieces) {
				piece.insertAfter(list, list.getLast());
			}
		} else {
			for (CodePiece piece : pieces) {
				AbstractInsnNode nodeAfter = location.getNext();
				piece.insertAfter(list, location);
				location = nodeAfter.getPrevious();
			}
		}
	}

	@Override
	public int size() {
		return sizeCache >= 0 ? sizeCache : (sizeCache = computeSize());
	}

	private int computeSize() {
		int size = 0;
		for (CodePiece piece : pieces) {
			size += piece.size();
		}
		return size;
	}

	@Override
	CodePiece append0(CodePiece other) {
		if (other instanceof CombinedCodePiece) {
			return new CombinedCodePiece(Iterables.concat(this.pieces, ((CombinedCodePiece) other).pieces));
		} else if (other instanceof MixedCombinedCodePiece) {
			return new MixedCombinedCodePiece(Iterables.concat(this.pieces, ((MixedCombinedCodePiece) other).elements));
		} else {
			return new CombinedCodePiece(Iterables.concat(this.pieces, Collections.singleton(other)));
		}
	}
}
