package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * <p>Represents a piece of Bytecode.</p>
 * @see de.take_weiland.mods.commons.asm.CodePieces The CodePieces class for working with CodePieces
 *
 * @author diesieben07
 */
public interface CodePiece {

	InsnList build();

	/**
	 * <p>Appends the instructions in this CodePiece to the given InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 * @param to the list to append to
	 */
	void appendTo(InsnList to);

	/**
	 * <p>Prepends the instructions in this CodePiece to the given InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 * @param to the list to append to
	 */
	void prependTo(InsnList to);

	/**
	 * <p>Inserts the instructions in this CodePiece after the given location, which must be part of the InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 * @param to the list to append to
	 * @param location the position where to insert the code, must be part of the InsnList
	 */
	void insertAfter(InsnList to, AbstractInsnNode location);

	/**
	 * <p>Inserts the instructions in this CodePiece before the given location, which must be part of the InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 * @param to the list to append to
	 * @param location the position where to insert the code, must be part of the InsnList
	 */
	void insertBefore(InsnList to, AbstractInsnNode location);

	/**
	 * <p>Inserts the instructions in this CodePiece after the given location.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 * @param to the list to append to
	 * @param location the position where to insert the code, must be part of the InsnList
	 */
	void insertAfter(CodeLocation location);

	/**
	 * <p>Inserts the instructions in this CodePiece before the given location.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 * @param to the list to append to
	 * @param location the position where to insert the code, must be part of the InsnList
	 */
	void insertBefore(CodeLocation location);

	/**
	 * <p>Replaces the given CodeLocation with the instructions in this CodePiece, making the location invalid.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 * @param to the list to append to
	 * @param location the position where to insert the code, must be part of the InsnList
	 */
	void replace(CodeLocation location);

	/**
	 * <p>Append the given instruction to this CodePiece.</p>
	 * <p>The instruction must not be used in any InsnList before or after this operation.</p>
	 * @param node the instruction to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the given instruction
	 */
	CodePiece append(AbstractInsnNode node);

	/**
	 * <p>Append the given InsnList to this CodePiece.</p>
	 * <p>The list must not be used elsewhere before or after this operation.</p>
	 * @param insns the list to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the instructions in the given InsnList.
	 */
	CodePiece append(InsnList insns);

	/**
	 * <p>Appends the given CodePiece to this CodePiece, leaving this and the other CodePiece intact and usable.</p>
	 * @param other the CodePiece to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the instructions in the given CodePiece.
	 */
	CodePiece append(CodePiece other);

	/**
	 * <p>Prepends the given CodePiece to this CodePiece, leaving this and the other CodePiece intact and usable.</p>
	 * <p>This method returns an equivalent result to {@code other.append(this)}</p>
	 * @param other the CodePiece to prepend
	 * @return a new CodePiece containing first the instructions in the given CodePiece and then the instructions in this CodePiece
	 */
	CodePiece prepend(CodePiece other);

	/**
	 * <p>Append the given InsnList to this CodePiece.</p>
	 * <p>The list must not be used elsewhere before or after this operation.</p>
	 * @param insns the list to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the instructions in the given InsnList.
	 */
	CodePiece prepend(AbstractInsnNode node);

	/**
	 * <p>Prepend the given instruction to this CodePiece.</p>
	 * <p>The instruction must not be used in any InsnList before or after this operation.</p>
	 * @param node the instruction to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the given instruction
	 */
	CodePiece prepend(InsnList insns);

	/**
	 * <p>Returns the number of instructions in this CodePiece.</p>
	 * @return the number of instructions
	 */
	int size();

}
