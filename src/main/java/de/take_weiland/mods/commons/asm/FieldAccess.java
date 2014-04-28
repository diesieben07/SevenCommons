package de.take_weiland.mods.commons.asm;

/**
 * Represents an ASM node that provides access to a value, usually a FieldNode or a getter/setter pair
 * @author diesieben07
 */
public interface FieldAccess {

	/**
	 * generate a new CodePiece that will load the value represented on the stack
	 * @return the CodePiece
	 */
	CodePiece getValue();

	/**
	 * generate a new CodePiece that will save a value to the field
	 * @param loadValue a CodePiece that loads the desired value onto the stack
	 * @return the CodePiece
	 * @throws java.lang.UnsupportedOperationException if {@link #isWritable()} is false
	 */
	CodePiece setValue(CodePiece loadValue);

	/**
	 * If this <tt>FieldAccess</tt> allows value-writes.
	 * @return true if writing is allowed
	 */
	boolean isWritable();

}
