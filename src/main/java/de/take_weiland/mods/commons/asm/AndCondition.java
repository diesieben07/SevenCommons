package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.LabelNode;

import java.util.Collections;
import java.util.List;

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
	void unwrapIntoOr(List<ASMCondition> list) {
		list.add(this);
	}

	@Override
	void unwrapIntoAnd(List<ASMCondition> list) {
		Collections.addAll(list, conditions);
	}

	private ASMCondition standardAnd(ASMCondition other) {
		ASMCondition[] all = new ASMCondition[conditions.length + 1];
		System.arraycopy(conditions, 0, all, 0, conditions.length);
		all[conditions.length] = other;
		return new AndCondition(all);
	}

}
