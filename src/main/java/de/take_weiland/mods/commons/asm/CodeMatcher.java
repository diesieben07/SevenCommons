package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.InsnList;

import java.util.List;

/**
 * @author diesieben07
 */
public interface CodeMatcher {

	CodeLocation findFirst(InsnList insns);

	CodeLocation findLast(InsnList insns);

	CodeLocation findOnly(InsnList insns);

	List<CodeLocation> findAll(InsnList insns);

	CodeLocation find(InsnList insns);

}
