package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.util.ComputingMap;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Represents a piece of Bytecode.</p>
 *
 * @author diesieben07
 * @see de.take_weiland.mods.commons.asm.CodePieces The CodePieces class for working with CodePieces
 */
public abstract class CodePiece {

	ContextKey contextKey = new ContextKey();

	CodePiece() { } // limit subclasses to package

	/**
	 * <p>Set the ContextKey used to compute the labels for this CodePiece.</p>
	 * <p>If a {@link org.objectweb.asm.tree.LabelNode} is shared across different CodePieces, the ContextKey for those
	 * CodePieces must be identical.</p>
	 * <p>The default ContextKey for a CodePiece is newly created and unique.</p>
	 * @param key the ContextKey
	 * @return this CodePiece
	 */
	public final CodePiece setContextKey(ContextKey key) {
		this.contextKey = checkNotNull(key, "key");
		return this;
	}

	/**
	 * <p>Appends the instructions in this CodePiece to the given InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 *
	 * @param to the list to append to
	 */
	public final void appendTo(InsnList to) {
		insertAfter(to, to.getLast());
	}

	/**
	 * <p>Prepends the instructions in this CodePiece to the given InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 *
	 * @param to the list to append to
	 */
	public final void prependTo(InsnList to) {
		insertBefore(to, to.getFirst());
	}

	/**
	 * <p>Inserts the instructions in this CodePiece after the given location, which must be part of the InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 *
	 * @param list       the list to append to
	 * @param location the position where to insert the code, must be part of the InsnList
	 */
	public final void insertAfter(InsnList list, AbstractInsnNode location) {
		insertAfter0(list, location, makeContext());
	}

	/**
	 * <p>Inserts the instructions in this CodePiece before the given location, which must be part of the InsnList.</p>
	 * <p>This method must only be used to build the "final" list of instructions for e.g. a method.
	 * Intermediate operations (e.g. concatenating various CodePieces) must use {@link #append(CodePiece)}, etc.</p>
	 *
	 * @param list       the list to append to
	 * @param location the position where to insert the code, must be part of the InsnList
	 */
	public final void insertBefore(InsnList list, AbstractInsnNode location) {
		insertBefore0(list, location, makeContext());
	}

	/**
	 * <p>Append the given instruction to this CodePiece.</p>
	 * <p>The instruction must not be used in any InsnList before or after this operation.</p>
	 *
	 * @param node the instruction to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the given instruction
	 */
	public final CodePiece append(AbstractInsnNode node) {
		return append(CodePieces.of(node));
	}

	/**
	 * <p>Append the given InsnList to this CodePiece.</p>
	 * <p>The list must not be used elsewhere before or after this operation.</p>
	 *
	 * @param insns the list to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the instructions in the given InsnList.
	 */
	public final CodePiece append(InsnList insns) {
		return append(CodePieces.of(insns));
	}

	/**
	 * <p>Appends the given CodePiece to this CodePiece, leaving this and the other CodePiece intact and usable.</p>
	 *
	 * @param other the CodePiece to append
	 * @return a new CodePiece containing first the instructions in this CodePiece and then the instructions in the given CodePiece.
	 */
	public final CodePiece append(CodePiece other) {
		return other.callRightAppend(this);
	}

	/**
	 * <p>Prepends the given CodePiece to this CodePiece, leaving this and the other CodePiece intact and usable.</p>
	 * <p>This method returns an equivalent result to {@code other.append(this)}</p>
	 *
	 * @param other the CodePiece to prepend
	 * @return a new CodePiece containing first the instructions in the given CodePiece and then the instructions in this CodePiece
	 */
	public final CodePiece prepend(CodePiece other) {
		return other.append(this);
	}

	/**
	 * <p>Prepend the given instruction to this CodePiece.</p>
	 * <p>The instruction must not be used in any InsnList before or after this operation.</p>
	 *
	 * @param node the instruction to prepend
	 * @return a new CodePiece containing first the given instruction and then the instructions in this CodePiece
	 */
	public final CodePiece prepend(AbstractInsnNode node) {
		return CodePieces.of(node).append(this);
	}

	/**
	 * <p>Prepend the given InsnList to this CodePiece.</p>
	 * <p>The list must not be used elsewhere before or after this operation.</p>
	 *
	 * @param insns the list to prepend
	 * @return a new CodePiece containing first the instructions in the given InsnList and then the instructions in this CodePiece
	 */
	public final CodePiece prepend(InsnList insns) {
		return CodePieces.of(insns).append(this);
	}

	void unwrapInto(Collection<? super CodePiece> coll) {
		coll.add(this);
	}

	CodePiece callRightAppend(CodePiece self) {
		return self.appendNormal(this);
	}

	CodePiece appendNormal(CodePiece other) {
		return new CombinedCodePiece(this, other);
	}

	CodePiece appendCombined(CombinedCodePiece other) {
		CodePiece[] all = new CodePiece[other.pieces.length + 1];
		all[0] = this;
		System.arraycopy(other.pieces, 0, all, 1, other.pieces.length);
		return new CombinedCodePiece(all);
	}

	boolean isCombined() {
		return false;
	}

	private static Function<LabelNode, LabelNode> labelCreator;
	static Map<LabelNode, LabelNode> newLabelMap() {
		if (labelCreator == null) {
			labelCreator = new Function<LabelNode, LabelNode>() {
				@Override
				public LabelNode apply(@Nullable LabelNode input) {
					return new LabelNode();
				}
			};
		}
		return ComputingMap.of(labelCreator);
	}

	private static Function<ContextKey, Map<LabelNode, LabelNode>> labelMapCreator;
	static Map<ContextKey, Map<LabelNode, LabelNode>> newContext() {
		if (labelMapCreator == null) {
			labelMapCreator = new Function<ContextKey, Map<LabelNode, LabelNode>>() {
				@Override
				public Map<LabelNode, LabelNode> apply(@Nullable ContextKey input) {
					return newLabelMap();
				}
			};
		}
		return ComputingMap.of(labelMapCreator);
	}

	static Map<ContextKey, Map<LabelNode, LabelNode>> newSimpleContext(ContextKey singleContext) {
		return ImmutableMap.of(singleContext, newLabelMap());
	}

	void insertBefore0(MethodNode method, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		insertBefore0(method.instructions, location, context);
	}

	void insertAfter0(MethodNode method, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		insertAfter0(method.instructions, location, context);
	}

	Map<ContextKey, Map<LabelNode, LabelNode>> makeContext() {
		return newSimpleContext(contextKey);
	}

	abstract void insertBefore0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context);

	abstract void insertAfter0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context);

}
