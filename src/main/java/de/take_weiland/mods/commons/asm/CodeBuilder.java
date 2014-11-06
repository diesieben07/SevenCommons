package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkState;

/**
 * <p>Helper class for building {@link de.take_weiland.mods.commons.asm.CodePiece CodePieces}.</p>
 * <p>CodeBuilder instances cannot be re-used when they have been built.</p>
 *
 * @author diesieben07
 */
public final class CodeBuilder {

	private ArrayList<CodePiece> pieces = Lists.newArrayList();

	/**
	 * <p>Add the given instruction to the list with the given ContextKey.</p>
	 * @param insn the instruction
	 * @param key the ContextKey
	 * @return this, for convenience
	 */
	public CodeBuilder add(AbstractInsnNode insn, ContextKey key) {
		checkNotBuilt();
		return add(CodePieces.of(insn).setContextKey(key));
	}

	/**
	 * <p>Add the given instruction to the list.</p>
	 * @param insn the instruction
	 * @return this, for convenience
	 */
	public CodeBuilder add(AbstractInsnNode insn) {
		checkNotBuilt();
		return add(CodePieces.of(insn));
	}

	/**
	 * <p>Add all instructions in the InsnList to the list with the given ContextKey.</p>
	 * @param list the instructions to add
	 * @param key the ContextKey
	 * @return this, for convenience
	 */
	public CodeBuilder add(InsnList list, ContextKey key) {
		checkNotBuilt();
		return add(CodePieces.of(list).setContextKey(key));
	}

	/**
	 * <p>Add all instructions in the InsnList to the list.</p>
	 * @param list the instructions to add
	 * @return this, for convenience
	 */
	public CodeBuilder add(InsnList list) {
		checkNotBuilt();
		return add(CodePieces.of(list));
	}

	/**
	 * <p>Add the given CodePiece to the list.</p>
	 * @param piece the CodePiece
	 * @return this, for convenience
	 */
	public CodeBuilder add(CodePiece piece) {
		checkNotBuilt();
		piece.unwrapInto(pieces);
		return this;
	}

	private void checkNotBuilt() {
		checkState(pieces != null, "Already built");
	}

	/**
	 * <p>Build this CodeBuilder into a CodePiece.</p>
	 * @return a CodePiece
	 */
	public CodePiece build() {
		ArrayList<CodePiece> list = pieces;
		pieces = null;
		switch (list.size()) {
			case 0:
				return CodePieces.of();
			case 1:
				return list.get(0);
			default:
				return new CombinedCodePiece(list.toArray(new CodePiece[list.size()]));
		}
	}

}
