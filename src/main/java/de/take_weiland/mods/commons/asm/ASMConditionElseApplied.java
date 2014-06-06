package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public interface ASMConditionElseApplied extends ASMConditionApplied {

	ASMConditionApplied then(CodePiece code);

}
