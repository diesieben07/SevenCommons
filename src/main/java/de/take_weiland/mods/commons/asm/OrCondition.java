package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.LabelNode;

import java.util.Collections;
import java.util.List;

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
		List<ASMCondition> inv = Lists.newArrayListWithExpectedSize(conditions.length);
		for (ASMCondition condition : conditions) {
			condition.negate().unwrapIntoAnd(inv);
		}
		return new AndCondition(inv.toArray(new ASMCondition[inv.size()]));
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
	void unwrapIntoOr(List<ASMCondition> list) {
		Collections.addAll(list, conditions);
	}

	@Override
	void unwrapIntoAnd(List<ASMCondition> list) {
		list.add(this);
	}

}