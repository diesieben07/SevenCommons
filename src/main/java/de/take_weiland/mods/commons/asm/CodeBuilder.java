package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;

/**
 * @author diesieben07
 */
public final class CodeBuilder {

	private final ArrayList<Object> objects = Lists.newArrayList();

	public CodeBuilder add(AbstractInsnNode insn) {
		objects.add(insn);
		return this;
	}

	public CodeBuilder add(InsnList list) {
		objects.add(list);
		return this;
	}

	public CodeBuilder add(CodePiece piece) {
		objects.add(piece);
		return this;
	}

	public CodePiece build() {
		objects.trimToSize();
		return new MixedCombinedCodePiece(objects);
	}

}
