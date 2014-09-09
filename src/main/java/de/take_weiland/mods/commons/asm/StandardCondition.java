package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

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
		return new StandardCondition(args, CodePieces.negateJmpOpcode(cmpOpcode));
	}

	@Override
	void jumpToIfTrue(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		cb.add(args);
		cb.add(new JumpInsnNode(cmpOpcode, lbl), context);
	}

	@Override
	void jumpToIfFalse(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		cb.add(args);
		cb.add(new JumpInsnNode(CodePieces.negateJmpOpcode(cmpOpcode), lbl), context);
	}

	@Override
	boolean preferJumpOnTrue() {
		return true;
	}

	@Override
	ASMCondition invokeOrCombination(ASMCondition self) {
		return self.orFromNormal(this);
	}

	@Override
	ASMCondition invokeAndCombination(ASMCondition self) {
		return self.andFromNormal(this);
	}

	@Override
	ASMCondition orFromNormal(StandardCondition other) {
		return new OrCondition(this, other);
	}

	@Override
	ASMCondition orFromOr(OrCondition other) {
		ASMCondition[] all = new ASMCondition[other.conditions.length + 1];
		all[0] = this;
		System.arraycopy(other.conditions, 0, all, 1, other.conditions.length);
		return new OrCondition(all);
	}

	@Override
	ASMCondition orFromAnd(AndCondition other) {
		return new OrCondition(this, other);
	}

	@Override
	ASMCondition andFromNormal(StandardCondition other) {
		return new AndCondition(this, other);
	}

	@Override
	ASMCondition andFromOr(OrCondition other) {
		return new AndCondition(this, other);
	}

	@Override
	ASMCondition andFromAnd(AndCondition other) {
		ASMCondition[] all = new ASMCondition[other.conditions.length + 1];
		all[0] = this;
		System.arraycopy(other.conditions, 0, all, 1, other.conditions.length);
		return new AndCondition(all);
	}
}
