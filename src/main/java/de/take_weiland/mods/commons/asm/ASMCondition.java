package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public interface ASMCondition {

	CodePiece ifTrue(CodePiece code);

	CodePiece ifFalse(CodePiece code);

	CodePiece doIfElse(CodePiece onTrue, CodePiece onFalse);

}
