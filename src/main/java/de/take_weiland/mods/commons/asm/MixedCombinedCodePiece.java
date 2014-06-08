package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author diesieben07
 */
class MixedCombinedCodePiece extends AbstractCodePiece {

	private final List<Object> elements;

	MixedCombinedCodePiece(Object... elements) {
		this.elements = Arrays.asList(elements);
	}

	MixedCombinedCodePiece(List<Object> elements) {
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
		if (location == to.getFirst()) {
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
				AbstractInsnNode beforeLocation = location.getPrevious();
				if (element instanceof AbstractInsnNode) {
					to.insertBefore(location, ((AbstractInsnNode) element).clone(context));
				} else if (element instanceof InsnList) {
					to.insertBefore(location, ASMUtils.clone((InsnList) element, context));
				} else if (element instanceof CodePiece) {
					((CodePiece) element).insertBefore(to, location);
				}
				location = beforeLocation.getNext();
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
}
