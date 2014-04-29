package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author diesieben07
 */
public interface CodeSearcher {

	CodeSearcher find(int opcode);

	CodeSearcher find(AbstractInsnNode insn, boolean lenient);

	CodeSearcher find(AbstractInsnNode insn);

	CodeSearcher find(Predicate<? super AbstractInsnNode> predicate);

	CodeSearcher find(Class<? extends AbstractInsnNode> type);

	<T extends AbstractInsnNode> CodeSearcher find(Class<T> type, Predicate<? super T> predicate);

	<S> CodeSearcher find(Function<? super AbstractInsnNode, ? extends S> function, Predicate<? super S> predicate);

	<T extends AbstractInsnNode, S> CodeSearcher find(Class<T> type, Function<? super T, ? extends S> function, Predicate<? super S> predicate);

	CodeSearcher jumpToStart();

	CodeSearcher jumpToEnd();

	CodeSearcher backwards();

	CodeSearcher forwards();

	CodeSearcher startHere();

	CodeLocation endHere();

	CodeSearcher reset();

}
