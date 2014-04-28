package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.List;

/**
 * @author diesieben07
 */
public interface CodeMatcher {

	CodeLocation findOnlyAfter(InsnList list, AbstractInsnNode first);

	CodeLocation findFirstAfter(InsnList list, AbstractInsnNode first);

	CodeLocation findLastAfter(InsnList list, AbstractInsnNode first);

	List<CodeLocation> findAllAfter(InsnList list, AbstractInsnNode first);

	CodeLocation findOnlyBefore(InsnList list, AbstractInsnNode last);

	CodeLocation findFirstBefore(InsnList list, AbstractInsnNode last);

	CodeLocation findLastBefore(InsnList list, AbstractInsnNode last);

	List<CodeLocation> findAllBefore(InsnList list, AbstractInsnNode last);

	CodeLocation findOnlyBetween(InsnList list, AbstractInsnNode first, AbstractInsnNode last);

	CodeLocation findFirstBetween(InsnList list, AbstractInsnNode first, AbstractInsnNode last);

	CodeLocation findLastBetween(InsnList list, AbstractInsnNode first, AbstractInsnNode last);

	List<CodeLocation> findAllBetween(InsnList list, AbstractInsnNode first, AbstractInsnNode last);

	/**
	 * Determine the size of this matcher
	 * If unknown, return -1
	 * @return the size of this matcher or -1 if unknown
	 */
	int size();

	CodeLocation findFirst(InsnList list);

	CodeLocation findLast(InsnList list);

	CodeLocation findOnly(InsnList list);

	List<CodeLocation> findAll(InsnList list);

	Chained andThen(CodeMatcher next);

	CodeMatcher onFailure(FailureAction action);

	FailureAction getFailureAction();

	boolean throwsExceptions();

	public static enum FailureAction {

		THROW,
		RETURN_NULL

	}

	public static interface Chained extends CodeMatcher {

		Chained allowSkipping();

		@Override
		Chained onFailure(FailureAction action);

	}

}
