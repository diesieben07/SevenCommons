package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.LabelNode;

/**
 * @author diesieben07
 */
class OrCondition extends ASMCondition {

	final ASMCondition[] conditions;

	OrCondition(ASMCondition... conditions) {
		this.conditions = conditions;
	}

	@Override
	public ASMCondition negate() {
		// de morgan
		ASMCondition[] inv = new ASMCondition[conditions.length];
		for (int i = 0; i < conditions.length; i++) {
			inv[i] = conditions[i].negate();
		}
		return new AndCondition(inv);
	}

	@Override
	void jumpToIfTrue(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		for (ASMCondition condition : conditions) {
			condition.jumpToIfTrue(cb, lbl, context);
		}
	}

	@Override
	void jumpToIfFalse(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		LabelNode afterChecks = new LabelNode();
		int last = conditions.length - 1;

		for (int i = 0; i < last; i++) {
			conditions[i].jumpToIfTrue(cb, afterChecks, context);
		}

		conditions[last].jumpToIfFalse(cb, lbl, context);

		cb.add(afterChecks, context);
	}

	@Override
	boolean preferJumpOnTrue() {
		return true;
	}

	@Override
	ASMCondition invokeOrCombination(ASMCondition self) {
		return self.orFromOr(this);
	}

	@Override
	ASMCondition invokeAndCombination(ASMCondition self) {
		return null;
	}

	@Override
	ASMCondition orFromNormal(StandardCondition other) {
		return standardOr(other);
	}

	@Override
	ASMCondition orFromAnd(AndCondition other) {
		return standardOr(other);
	}

	private ASMCondition standardOr(ASMCondition other) {
		ASMCondition[] all = new ASMCondition[conditions.length + 1];
		System.arraycopy(conditions, 0, all, 0, conditions.length);
		all[conditions.length] = other;
		return new OrCondition(all);
	}

	@Override
	ASMCondition orFromOr(OrCondition other) {
		ASMCondition[] all = new ASMCondition[other.conditions.length + this.conditions.length];
		System.arraycopy(this.conditions, 0, all, 0, this.conditions.length);
		System.arraycopy(other.conditions, 0, all, this.conditions.length, other.conditions.length);
		return new OrCondition(all);
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
