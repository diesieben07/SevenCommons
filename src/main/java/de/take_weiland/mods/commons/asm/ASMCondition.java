package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public interface ASMCondition {

	ASMConditionThenApplied then(CodePiece code);

	ASMConditionElseApplied otherwise(CodePiece code);

}
