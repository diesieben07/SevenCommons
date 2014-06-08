package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public interface ASMCondition {

	ASMCondition negate();

	ASMConditionThenApplied then(CodePiece code);

	ASMConditionElseApplied otherwise(CodePiece code);

}
