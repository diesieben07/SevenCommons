package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author diesieben07
 */
class CombinedCodePiece extends CodePiece {

	final CodePiece[] pieces;

	CombinedCodePiece(CodePiece... pieces) {
		this.pieces = pieces;
	}

	@Override
	public void insertBefore(InsnList list, AbstractInsnNode location) {
		insertBefore0(list, location, newContext());
	}

	@Override
	public void insertAfter(InsnList list, AbstractInsnNode location) {
		insertAfter0(list, location, newContext());
	}

	@Override
	void insertBefore0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		for (CodePiece piece : pieces) {
			piece.insertBefore0(insns, location, context);
		}
	}

	@Override
	void insertAfter0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		if (location == insns.getLast()) { // special case for empty list
			for (CodePiece piece : pieces) {
				piece.insertAfter0(insns, insns.getLast(), context);
			}
		} else {
			for (CodePiece piece : pieces) {
				AbstractInsnNode nodeAfter = location.getNext();
				piece.insertAfter0(insns, location, context);
				location = nodeAfter.getPrevious();
			}
		}
	}

	@Override
	void unwrapInto(Collection<? super CodePiece> coll) {
		Collections.addAll(coll, pieces);
	}

	@Override
	CodePiece callRightAppend(CodePiece self) {
		return self.appendCombined(this);
	}

	@Override
	CodePiece appendNormal(CodePiece other) {
		CodePiece[] all = new CodePiece[pieces.length + 1];
		System.arraycopy(pieces, 0, all, 0, pieces.length);
		all[pieces.length] = other;
		return new CombinedCodePiece(all);
	}

	@Override
	CodePiece appendCombined(CombinedCodePiece other) {
		CodePiece[] all = new CodePiece[pieces.length + other.pieces.length];
		System.arraycopy(pieces, 0, all, 0, pieces.length);
		System.arraycopy(other.pieces, 0, all, pieces.length, other.pieces.length);
		return new CombinedCodePiece(all);
	}

	@Override
	boolean isCombined() {
		return true;
	}

	@Override
	boolean isEmpty() {
		for (CodePiece piece : pieces) {
			if (!piece.isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
