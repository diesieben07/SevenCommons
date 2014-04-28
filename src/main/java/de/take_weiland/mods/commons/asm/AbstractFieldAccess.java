package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
abstract class AbstractFieldAccess implements FieldAccess {

	private CodePiece get;

	@Override
	public final CodePiece getValue() {
		return (get == null) ? (get = makeGet())  : get;
	}

	@Override
	public CodePiece setValue(CodePiece loadValue) {
		throw new UnsupportedOperationException();
	}

	abstract CodePiece makeGet();

}
