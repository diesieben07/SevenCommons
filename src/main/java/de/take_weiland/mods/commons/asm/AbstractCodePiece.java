package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.util.ComputingMap;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Abstract base class for CodePieces.<br/>
 * // TODO
 * {@link #insertBefore(org.objectweb.asm.tree.InsnList, org.objectweb.asm.tree.AbstractInsnNode)},
 * {@link #insertAfter(org.objectweb.asm.tree.InsnList, org.objectweb.asm.tree.AbstractInsnNode)},
 * {@link #appendTo(org.objectweb.asm.tree.InsnList)} and
 * {@link #prependTo(org.objectweb.asm.tree.InsnList)}
 * can (and should) be overridden if the creation of a new InsnList for this operation is not the best way
 * (e.g. CodePieces that contain only a single instruction.
 *
 * @author diesieben07
 */
public abstract class AbstractCodePiece implements CodePiece {

	private static Map<InsnList, Map<LabelNode, LabelNode>> contexts;

	protected static Map<LabelNode, LabelNode> persistentContext(InsnList insns) {
		if (contexts == null) {
			contexts = new WeakHashMap<>();
		}
		Map<LabelNode, LabelNode> context = contexts.get(insns);
		if (context == null) {
			contexts.put(insns, (context = Maps.newHashMap()));
		}
		return context;
	}

	private static Function<LabelNode, LabelNode> instanceProvider;

	protected static Map<LabelNode, LabelNode> newContext() {
		if (instanceProvider == null) {
			instanceProvider = new Function<LabelNode, LabelNode>() {
				@Override
				public LabelNode apply(LabelNode input) {
					return new LabelNode();
				}
			};
		}
		return ComputingMap.of(instanceProvider);
	}

	@Override
	public final void prependTo(InsnList to) {
		insertBefore(to, to.getFirst());
	}

	@Override
	public final void appendTo(InsnList to) {
		insertAfter(to, to.getLast());
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
	public final void replace(CodeLocation location) {
		if (location.first() == location.list().getFirst()) {
			JavaUtils.clear(location);
			prependTo(location.list());
		} else {
			AbstractInsnNode firstBefore = location.first().getPrevious();
			JavaUtils.clear(location);
			insertAfter(location.list(), firstBefore);
		}
	}

	@Override
	public final CodePiece append(CodePiece other) {
		if (other instanceof CombinedCodePiece) {
			return new CombinedCodePiece(ObjectArrays.concat(this, ((CombinedCodePiece) other).pieces));
		} else {
			return new CombinedCodePiece(new CodePiece[] { this, other });
		}
	}

	@Override
	public final CodePiece append(AbstractInsnNode node) {
		return append(CodePieces.of(node));
	}

	@Override
	public final CodePiece append(InsnList insns) {
		return append(CodePieces.of(insns));
	}

	@Override
	public final CodePiece prepend(CodePiece other) {
		return other.append(this);
	}

	@Override
	public CodePiece prepend(AbstractInsnNode node) {
		return CodePieces.of(node).append(this);
	}

	@Override
	public CodePiece prepend(InsnList insns) {
		return CodePieces.of(insns).append(this);
	}
}
