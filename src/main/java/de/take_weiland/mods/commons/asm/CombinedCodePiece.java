package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author diesieben07
 */
class CombinedCodePiece extends AbstractCodePiece {

	final CodePiece[] pieces;
	private int sizeCache = -1;

	CombinedCodePiece(CodePiece[] pieces) {
		this.pieces = pieces;
	}

	@Override
	public void insertBefore(InsnList list, AbstractInsnNode location) {
		if (location == list.getFirst()) {
			for (int i = pieces.length - 1; i >= 0; --i) {
				pieces[i].insertBefore(list, list.getFirst());
			}
		} else {
			for (int i = pieces.length - 1; i >= 0; i--) {
				AbstractInsnNode nodeBefore = location.getPrevious();
				pieces[i].insertBefore(list, location);
				location = nodeBefore.getNext();
			}
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
}
