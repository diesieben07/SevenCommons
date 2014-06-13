package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Iterables;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author diesieben07
 */
class MixedCombinedCodePiece extends AbstractCodePiece {

	final Iterable<Object> elements;

	MixedCombinedCodePiece(Object... elements) {
		this.elements = Arrays.asList(elements);
	}

	MixedCombinedCodePiece(Iterable<Object> elements) {
		this.elements = elements;
	}

	@Override
	public void insertAfter(InsnList to, AbstractInsnNode location) {
		Map<LabelNode, LabelNode> context = newContext();
		if (location == to.getLast()) {
			for (Object element : elements) {
				if (element instanceof AbstractInsnNode) {
					to.add(((AbstractInsnNode) element).clone(context));
				} else if (element instanceof InsnList) {
					to.add(ASMUtils.clone((InsnList) element, context));
				} else if (element instanceof CodePiece) {
					((CodePiece) element).appendTo(to);
				}
			}
		} else {
			for (Object element : elements) {
				AbstractInsnNode afterLocation = location.getNext();
				if (element instanceof AbstractInsnNode) {
					to.insert(location, ((AbstractInsnNode) element).clone(context));
				} else if (element instanceof InsnList) {
					InsnList clone = ASMUtils.clone((InsnList) element, context);
					to.insert(location, clone);
				} else if (element instanceof CodePiece) {
					((CodePiece) element).insertAfter(to, location);
				} else {
					throw invalidElement();
				}
				location = afterLocation.getPrevious();
			}
		}
	}

	@Override
	public void insertBefore(InsnList to, AbstractInsnNode location) {
		Map<LabelNode, LabelNode> context = newContext();
		if (location == null) {
			for (Object element : elements) {
				if (element instanceof AbstractInsnNode) {
					to.insert(((AbstractInsnNode) element).clone(context));
				} else if (element instanceof InsnList) {
					to.insert(ASMUtils.clone((InsnList) element, context));
				} else if (element instanceof CodePiece) {
					((CodePiece) element).prependTo(to);
				}
			}
		} else {
			for (Object element : elements) {
				if (element instanceof AbstractInsnNode) {
					to.insertBefore(location, ((AbstractInsnNode) element).clone(context));
				} else if (element instanceof InsnList) {
					to.insertBefore(location, ASMUtils.clone((InsnList) element, context));
				} else if (element instanceof CodePiece) {
					((CodePiece) element).insertBefore(to, location);
				}
			}
		}
	}

	private int sizeCache = -1;

	@Override
	public int size() {
		return sizeCache < 0 ? (sizeCache = computeSize()) : sizeCache;
	}

	private int computeSize() {
		int size = 0;
		for (Object o : elements) {
			if (o instanceof AbstractInsnNode) {
				++size;
			} else if (o instanceof InsnList) {
				size += ((InsnList) o).size();
			} else if (o instanceof CodePiece) {
				size += ((CodePiece) o).size();
			} else {
				throw invalidElement();
			}
		}
		return size;
	}

	private static RuntimeException invalidElement() {
		return new IllegalStateException("Invalid Element in MixedCombinedCodePiece!");
	}

	@Override
	public CodePiece append0(CodePiece other) {
		if (other instanceof CombinedCodePiece) {
			return new MixedCombinedCodePiece(Iterables.concat(this.elements, ((CombinedCodePiece) other).pieces));
		} else if (other instanceof MixedCombinedCodePiece) {
			return new CombinedCodePiece(this, other);
		} else {
			return new MixedCombinedCodePiece(Iterables.concat(this.elements, Collections.singleton(other)));
		}
	}

}
