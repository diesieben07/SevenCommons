package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public interface ASMCondition {

	ASMCondition negate();

	CodePiece makeDoWhile(CodePiece code);

	CodePiece makeWhile(CodePiece code);

	ASMConditionThenApplied then(CodePiece code);

	ASMConditionElseApplied otherwise(CodePiece code);

}
