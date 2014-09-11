package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

/**
 * @author diesieben07
 */
class StandardCondition extends ASMCondition {

	private final CodePiece args;
	private final int cmpOpcode;

	StandardCondition(CodePiece args, int cmpOpcode) {
		this.args = args;
		this.cmpOpcode = cmpOpcode;
	}

	@Override
	public ASMCondition negate() {
		return new StandardCondition(args, negateJmpOpcode(cmpOpcode));
	}

	@Override
	void jumpToIfTrue(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		cb.add(args);
		cb.add(new JumpInsnNode(cmpOpcode, lbl), context);
	}

	@Override
	void jumpToIfFalse(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		cb.add(args);
		cb.add(new JumpInsnNode(negateJmpOpcode(cmpOpcode), lbl), context);
	}

	@Override
	boolean preferJumpOnTrue() {
		return true;
	}

	@Override
	void unwrapIntoOr(List<ASMCondition> list) {
		list.add(this);
	}

	@Override
	void unwrapIntoAnd(List<ASMCondition> list) {
		list.add(this);
	}

}
