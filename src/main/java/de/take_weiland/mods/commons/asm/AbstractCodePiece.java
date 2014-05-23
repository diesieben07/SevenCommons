package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * <p>Abstract base class for CodePieces.</p>
 * <p>At least {@link #build()} must be implemented, but
 * {@link #insertBefore(org.objectweb.asm.tree.InsnList, org.objectweb.asm.tree.AbstractInsnNode)},
 * {@link #insertAfter(org.objectweb.asm.tree.InsnList, org.objectweb.asm.tree.AbstractInsnNode)},
 * {@link #appendTo(org.objectweb.asm.tree.InsnList)} and
 * {@link #prependTo(org.objectweb.asm.tree.InsnList)}
 * can (and should) be overridden if the creation of a new InsnList for this operation is not the best way
 * (e.g. CodePieces that contain only a single instruction.</p>
 *
 * @author diesieben07
 */
public abstract class AbstractCodePiece implements CodePiece {

	@Override
	public void insertAfter(InsnList into, AbstractInsnNode location) {
		into.insert(location, build());
	}

	@Override
	public void insertBefore(InsnList into, AbstractInsnNode location) {
		into.insertBefore(location, build());
	}

	@Override
	public void appendTo(InsnList to) {
		to.add(build());
	}

	@Override
	public void prependTo(InsnList to) {
		to.insert(build());
	}

	@Override
	public void appendTo(MethodVisitor mv) {
		build().accept(mv);
	}

	@Override
	public final void insertBefore(CodeLocation location) {
		insertBefore(location.list(), location.first());
	}

	@Override
	public final void insertAfter(CodeLocation location) {
		insertAfter(location.list(), location.last());
	}

	@Override
	public void replace(CodeLocation location) {
		AbstractInsnNode firstBefore = location.first().getPrevious();
		JavaUtils.clear(location);
		if (firstBefore == null) {
			prependTo(location.list());
		} else {
			insertAfter(location.list(), firstBefore);
		}
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
