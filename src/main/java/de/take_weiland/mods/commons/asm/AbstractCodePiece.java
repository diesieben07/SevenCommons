package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.util.ComputingMap;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Abstract base class for CodePieces.<br/>
 * At least {@link #build()} must be implemented, but
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

	private static Map<InsnList, Map<LabelNode, LabelNode>> labelsPerList;
	private static Function<Object, LabelNode> instanceProvider;

	protected static Map<LabelNode, LabelNode> cloneMapFor(InsnList list) {
		if (labelsPerList == null) {
			labelsPerList = new WeakHashMap<>();
			instanceProvider = new Function<Object, LabelNode>() {
				@Override
				public LabelNode apply(Object input) {
					return new LabelNode();
				}
			};
		}
		Map<LabelNode, LabelNode> labels = labelsPerList.get(list);
		if (labels == null) {
			labelsPerList.put(list, (labels = ComputingMap.create(instanceProvider)));
		}
		return labels;
	}

	protected static LabelNode cloneFor(InsnList list, LabelNode label) {
		Map<LabelNode, LabelNode> map = cloneMapFor(list);
		LabelNode clone = map.get(label);
		if (clone == null) {
			map.put(label, (clone = new LabelNode()));
		}
		return clone;
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
