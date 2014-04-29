package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author diesieben07
 */
public interface CodePiece {

	InsnList build();

	void appendTo(InsnList to);

	void prependTo(InsnList to);

	void insertAfter(InsnList to, AbstractInsnNode location);

	void insertBefore(InsnList to, AbstractInsnNode location);

	void insertAfter(CodeLocation location);

	void insertBefore(CodeLocation location);

	void replace(CodeLocation location);

	CodePiece append(CodePiece other);

	CodePiece prepend(CodePiece other);

	int size();

}
