package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.objectweb.asm.Type;

import java.util.BitSet;

import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
class BitSetHandler extends PropertyHandler {

	private ASMVariable companion;

	BitSetHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifEqual(var.get(), companion.get(), Type.getType(BitSet.class)).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "writeBitSet";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(BitSet.class));
		CodePiece write = CodePieces.invokeVirtual(owner, name, desc, stream, var.get());

		return write.append(canBeNull ? updateNullable() : updateNotNull());
	}

	private CodePiece updateNotNull() {
		return ASMCondition.ifNull(companion.get()).doIfElse(cloneSet(), copySet());
	}

	private CodePiece updateNullable() {
		return ASMCondition.ifNull(var.get()).doIfElse(setCompanionNull(), updateNotNull());
	}

	private CodePiece copySet() {
		String owner = Type.getInternalName(BitSet.class);
		String name = "clear";
		String desc = Type.getMethodDescriptor(VOID_TYPE);
		CodePiece clear = CodePieces.invokeVirtual(owner, name, desc, companion.get());

		name = "or";
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(BitSet.class));
		CodePiece or = CodePieces.invokeVirtual(owner, name, desc, companion.get(), var.get());

		return clear.append(or);
	}

	private CodePiece cloneSet() {
		String owner = Type.getInternalName(BitSet.class);
		String name = "clone";
		String desc = Type.getMethodDescriptor(getType(Object.class));
		CodePiece clone = CodePieces.castTo(BitSet.class, CodePieces.invokeVirtual(owner, name, desc, var.get()));
		return companion.set(clone);
	}

	private CodePiece setCompanionNull() {
		return companion.set(CodePieces.constantNull());
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readBitSet";
		String desc = Type.getMethodDescriptor(getType(BitSet.class), getType(BitSet.class));
		return var.set(CodePieces.invokeVirtual(owner, name, desc, stream, var.get()));
	}
}
