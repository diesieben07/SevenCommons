package de.take_weiland.mods.commons.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
class ConditionImpl implements ASMCondition, ASMConditionElseApplied, ASMConditionThenApplied {

	private final CodePiece conditionArgs;
	private final int trueOpcode;
	private final int falseOpcode;


	private CodePiece onThen;
	private CodePiece onElse;

	ConditionImpl(CodePiece conditionArgs, int trueOpcode, int falseOpcode) {
		this.conditionArgs = conditionArgs;
		this.trueOpcode = trueOpcode;
		this.falseOpcode = falseOpcode;
	}

	@Override
	public ASMConditionThenApplied then(@NotNull CodePiece code) {
		checkUse(onThen == null);
		onThen = checkNotNull(code);
		return this;
	}

	@Override
	public ASMConditionElseApplied otherwise(@NotNull CodePiece code) {
		checkUse(onElse == null);
		onElse = checkNotNull(code);
		return this;
	}

	@Override
	public CodePiece build() {
		boolean hasElse = onElse != null;
		boolean hasThen = onThen != null;
		checkUse(hasElse || hasThen);

		if (hasElse && hasThen) {
			LabelNode after = new LabelNode();
			LabelNode isFalse = new LabelNode();
			return new MixedCombinedCodePiece(
					conditionArgs,
					new JumpInsnNode(falseOpcode, isFalse),
					onThen,
					new JumpInsnNode(GOTO, after),
					isFalse,
					onElse,
					after);
		} else if (hasElse) {
			return end0(trueOpcode, onElse);
		} else {
			return end0(falseOpcode, onThen);
		}
	}

	private CodePiece end0(int invOpcode, CodePiece code) {
		LabelNode after = new LabelNode();
		return new MixedCombinedCodePiece(
				conditionArgs,
				new JumpInsnNode(invOpcode, after),
				code,
				after);
	}

	private static void checkUse(boolean cond) {
		checkState(cond, "Incorrect use of ASMCondition interface!");
	}
}