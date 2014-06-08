package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
interface CodePieceInternal {

	CodePiece callProperAppend(CodePieceInternal origin);

	CodePiece append0(CodePiece piece);

	CodePiece append0(CombinedCodePiece piece);

	CodePiece append0(MixedCombinedCodePiece piece);

}
