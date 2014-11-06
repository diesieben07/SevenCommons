package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;

/**
 * <p>A placeholder for a CodePiece. The actual code is resolved once the CodePiece is appended to an InsnList.</p>
 * @author diesieben07
 */
public abstract class CodePlaceholder extends CodePiece {

	/**
	 * <p>Resolve this placeholder to the actual CodePiece. This method is called at most once per instance.</p>
	 * @return the resolved CodePiece
	 */
	protected abstract CodePiece resolve();

	private CodePiece delegate;

	private CodePiece delegate() {
		return delegate == null ? (delegate = resolve()) : delegate;
	}

	@Override
	public final void insertAfter(InsnList to, AbstractInsnNode location) {
		delegate().insertAfter(to, location);
	}

	@Override
	public final void insertBefore(InsnList to, AbstractInsnNode location) {
		delegate().insertBefore(to, location);
	}

	@Override
	void insertBefore0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		delegate().insertBefore0(insns, location, context);
	}

	@Override
	void insertAfter0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		delegate().insertAfter0(insns, location, context);
	}

	@Override
	boolean isEmpty() {
		return delegate().isEmpty();
	}

	protected CodePlaceholder() { }
}