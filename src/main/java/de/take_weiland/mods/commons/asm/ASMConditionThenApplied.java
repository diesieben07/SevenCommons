package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public interface ASMConditionThenApplied extends ASMConditionApplied {

	ASMConditionApplied otherwise(CodePiece piece);

}
