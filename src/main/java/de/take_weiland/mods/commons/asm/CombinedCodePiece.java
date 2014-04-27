package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ObjectArrays;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author diesieben07
 */
class CombinedCodePiece extends AbstractCodePiece {

	final CodePiece[] pieces;
	private int sizeCache = -1;

	CombinedCodePiece(CodePiece... pieces) {
		this.pieces = pieces;
	}

	@Override
	public InsnList build() {
		InsnList result = new InsnList();
		for (CodePiece piece : pieces) {
			piece.appendTo(result);
		}
		return result;
	}

	@Override
	public void insertAfter(InsnList into, AbstractInsnNode location) {
		for (CodePiece piece : pieces) {
			piece.insertAfter(into, location);
			location = ASMUtils.advance(location, piece.size());
		}
	}

	@Override
	public void insertBefore(InsnList into, AbstractInsnNode location) {
		for (int i = pieces.length - 1; i >= 0; --i) {
			CodePiece piece = pieces[i];
			piece.insertBefore(into, location);
			location = ASMUtils.getPrevious(location, piece.size());
		}
	}

	@Override
	public void appendTo(InsnList to) {
		for (CodePiece piece : pieces) {
			piece.appendTo(to);
		}
	}

	@Override
	public void prependTo(InsnList to) {
		for (int i = pieces.length - 1; i >= 0; --i) {
			pieces[i].prependTo(to);
		}
	}

	@Override
	public CodePiece append(CodePiece other) {
		if (other instanceof CombinedCodePiece) {
			return new CombinedCodePiece(ObjectArrays.concat(pieces, ((CombinedCodePiece) other).pieces, CodePiece.class));
		} else {
			return new CombinedCodePiece(ObjectArrays.concat(pieces, other));
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
