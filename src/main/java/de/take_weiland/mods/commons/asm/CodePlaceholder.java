package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * <p>A placeholder for a CodePiece. The actual code is resolved once the CodePiece is appended to an InsnList.</p>
 * @author diesieben07
 */
public abstract class CodePlaceholder extends CodePiece {

	private final Map<MethodNode, CodePiece> cache = new IdentityHashMap<>();

	/**
	 * <p>Resolve this placeholder to the actual CodePiece. This method is called at most once per instance and target method.</p>
	 * @return the resolved CodePiece
	 * @param method the current method, may be null if unknown
	 */
	protected abstract CodePiece resolve(@Nullable MethodNode method);

	private CodePiece delegate(@Nullable MethodNode method) {
		CodePiece resolved = cache.get(method);
		if (resolved == null) {
			resolved = resolve(method);
			cache.put(method, resolved);
		}
		return resolved;
	}

	@Override
	void insertBefore0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		delegate(null).insertBefore0(insns, location, context);
	}

	@Override
	void insertAfter0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		delegate(null).insertAfter0(insns, location, context);
	}

	protected CodePlaceholder() { }
}