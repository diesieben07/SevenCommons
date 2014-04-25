package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
abstract class AbstractFieldAccess implements FieldAccess {

	private CodePiece get;
	private CodePiece set;

	@Override
	public final CodePiece getValue() {
		return (get == null) ? (get = makeGet())  : get;
	}

	@Override
	public final CodePiece setValue() {
		if (!isWritable()) {
			throw new UnsupportedOperationException();
		}
		return (set == null) ? (set = makeSet()) : set;
	}

	abstract CodePiece makeGet();
	CodePiece makeSet() { throw new AssertionError(); }

}
