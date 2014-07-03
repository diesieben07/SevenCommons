package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.util.ComputingMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Collections;
import java.util.Map;

abstract class AbstractCodePiece implements CodePiece {

	private static Function<LabelNode, LabelNode> instanceProvider;

	static Map<LabelNode, LabelNode> newContext() {
		if (instanceProvider == null) {
			instanceProvider = new Function<LabelNode, LabelNode>() {
				@Override
				public LabelNode apply(LabelNode input) {
					return new LabelNode();
				}
			};
		}
		return ComputingMap.of(instanceProvider);
	}

	@Override
	public InsnList build() {
		InsnList list = new InsnList();
		appendTo(list);
		return list;
	}

	@Override
	public final void prependTo(InsnList to) {
		insertBefore(to, to.getFirst());
	}

	@Override
	public final void appendTo(InsnList to) {
		insertAfter(to, to.getLast());
	}

	@Override
	public final CodePiece append(CodePiece other) {
		return append0(other);
	}

	CodePiece append0(CodePiece other) {
		if (other instanceof CombinedCodePiece) {
			return new CombinedCodePiece(Iterables.concat(Collections.singleton(this), ((CombinedCodePiece) other).pieces));
		} else if (other instanceof MixedCombinedCodePiece) {
			return new MixedCombinedCodePiece(Iterables.concat(Collections.singleton(this), ((MixedCombinedCodePiece) other).elements));
		} else {
			return new CombinedCodePiece(this, other);
		}
	}

	@Override
	public final CodePiece append(AbstractInsnNode node) {
		return append(CodePieces.of(node));
	}

	@Override
	public final CodePiece append(InsnList insns) {
		return append(CodePieces.of(insns));
	}

	@Override
	public final CodePiece prepend(CodePiece other) {
		return other.append(this);
	}

	@Override
	public CodePiece prepend(AbstractInsnNode node) {
		return CodePieces.of(node).append(this);
	}

	@Override
	public CodePiece prepend(InsnList insns) {
		return CodePieces.of(insns).append(this);
	}


}
