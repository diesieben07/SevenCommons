package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public interface ParameterizedCodePiece {

	CodePiece apply(CodePiece... args);

}
