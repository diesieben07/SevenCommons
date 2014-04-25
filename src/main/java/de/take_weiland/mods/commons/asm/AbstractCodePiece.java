package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ObjectArrays;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author diesieben07
 */
public abstract class AbstractCodePiece implements CodePiece {

	@Override
	public void appendTo(InsnList to) {
		to.add(build());
	}

	@Override
	public void prependTo(InsnList to) {
		to.insert(build());
	}

	@Override
	public void insertAfter(InsnList into, AbstractInsnNode location) {
		into.insert(location, build());
	}

	@Override
	public void insertBefore(InsnList into, AbstractInsnNode location) {
		into.insertBefore(location, build());
	}

	@Override
	public CodePiece append(CodePiece other) {
		if (other instanceof CombinedCodePiece) {
			return new CombinedCodePiece(ObjectArrays.concat(this, ((CombinedCodePiece) other).pieces));
		} else {
			return new CombinedCodePiece(this, other);
		}
	}

	@Override
	public final CodePiece prepend(CodePiece other) {
		return other.append(this);
	}
}
