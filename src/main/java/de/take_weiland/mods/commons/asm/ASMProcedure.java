package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;

/**
 * @author diesieben07
 */
public interface ASMProcedure extends Function<CodePiece[], CodePiece> {

	@Override
	CodePiece apply(CodePiece... input);
}
