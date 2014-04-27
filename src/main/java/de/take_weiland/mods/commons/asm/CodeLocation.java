package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author diesieben07
 */
public final class CodeLocation {

	private final InsnList list;
	private final AbstractInsnNode start;
	private final AbstractInsnNode end;

	public CodeLocation(InsnList list, AbstractInsnNode start, AbstractInsnNode end) {
		this.list = list;
		this.start = start;
		this.end = end;
	}

	public CodeLocation(InsnList list, AbstractInsnNode insn) {
		this(list, insn, insn);
	}

	public AbstractInsnNode start() {
		return start;
	}

	public AbstractInsnNode end() {
		return end;
	}

	public InsnList list() {
		return list;
	}
}
