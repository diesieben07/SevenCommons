package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public final class CodeBuilder {

	private ArrayList<CodePiece> pieces = Lists.newArrayList();

	public CodeBuilder add(@NotNull AbstractInsnNode insn, ContextKey key) {
		checkNotBuilt();
		return add(CodePieces.of(insn).setContextKey(key));
	}

	public CodeBuilder add(@NotNull AbstractInsnNode insn) {
		checkNotBuilt();
		return add(CodePieces.of(insn));
	}

	public CodeBuilder add(@NotNull InsnList list, ContextKey key) {
		checkNotBuilt();
		return add(CodePieces.of(list).setContextKey(key));
	}

	public CodeBuilder add(@NotNull InsnList list) {
		checkNotBuilt();
		return add(CodePieces.of(list));
	}

	public CodeBuilder add(@NotNull CodePiece piece) {
		checkNotBuilt();
		piece.unwrapInto(pieces);
		return this;
	}

	private void checkNotBuilt() {
		checkState(pieces != null, "Already built");
	}

	public CodePiece build() {
		ArrayList<CodePiece> list = pieces;
		pieces = null;
		return new CombinedCodePiece(list.toArray(new CodePiece[list.size()]));
	}

}
