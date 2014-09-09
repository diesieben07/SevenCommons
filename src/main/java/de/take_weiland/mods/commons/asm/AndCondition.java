package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.LabelNode;

/**
 * @author diesieben07
 */
class AndCondition extends ASMCondition {

	final ASMCondition[] conditions;

	AndCondition(ASMCondition... conditions) {
		this.conditions = conditions;
	}

	@Override
	public ASMCondition negate() {
		// de morgan
		ASMCondition[] inv = new ASMCondition[conditions.length];
		for (int i = 0; i < conditions.length; i++) {
			inv[i] = conditions[i].negate();
		}
		return new OrCondition(inv);
	}

	@Override
	void jumpToIfTrue(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		LabelNode afterChecks = new LabelNode();

		int last = conditions.length - 1;
		for (int i = 0; i < last; i++) {
			ASMCondition condition = conditions[i];
			condition.jumpToIfFalse(cb, afterChecks, context);
		}

		conditions[last].jumpToIfTrue(cb, lbl, context);

		cb.add(afterChecks, context);
	}

	@Override
	void jumpToIfFalse(CodeBuilder cb, LabelNode lbl, ContextKey context) {
		for (ASMCondition condition : conditions) {
			condition.jumpToIfFalse(cb, lbl, context);
		}
	}

	@Override
	boolean preferJumpOnTrue() {
		return false;
	}

	@Override
	ASMCondition invokeOrCombination(ASMCondition self) {
		return self.orFromAnd(this);
	}

	@Override
	ASMCondition invokeAndCombination(ASMCondition self) {
		return self.andFromAnd(this);
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
		return standardAnd(other);
	}

	@Override
	ASMCondition andFromOr(OrCondition other) {
		return standardAnd(other);
	}

	private ASMCondition standardAnd(ASMCondition other) {
		ASMCondition[] all = new ASMCondition[conditions.length + 1];
		System.arraycopy(conditions, 0, all, 0, conditions.length);
		all[conditions.length] = other;
		return new AndCondition(all);
	}

	@Override
	ASMCondition andFromAnd(AndCondition other) {
		ASMCondition[] all = new ASMCondition[this.conditions.length + other.conditions.length];
		System.arraycopy(this.conditions, 0, all, 0, this.conditions.length);
		System.arraycopy(other.conditions, 0, all, this.conditions.length, other.conditions.length);
		return new AndCondition(all);
	}
}
