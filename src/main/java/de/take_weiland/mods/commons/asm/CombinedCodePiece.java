package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ObjectArrays;
import org.objectweb.asm.tree.InsnList;

/**
 * @author diesieben07
 */
class CombinedCodePiece extends AbstractCodePiece {

	final CodePiece[] pieces;

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
	public void appendTo(InsnList to) {
		for (CodePiece piece : pieces) {
			piece.appendTo(to);
		}
	}

	@Override
	public void prependTo(InsnList to) {
		// need to iterate in reverse order to preserve order of the children
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
}
