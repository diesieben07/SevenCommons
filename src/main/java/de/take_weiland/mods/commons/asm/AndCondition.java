package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Lists;
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
		List<ASMCondition> inv = Lists.newArrayListWithExpectedSize(conditions.length);
		for (int i = 0; i < conditions.length; i++) {
			conditions[i].negate().unwrapIntoOr(inv);
		}
		return new OrCondition(inv.toArray(new ASMCondition[inv.size()]));
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

}
