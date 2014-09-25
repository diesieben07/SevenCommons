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

		ASMCondition varNull = ASMCondition.ifNull(var.get());
		ASMCondition compNull = ASMCondition.ifNull(companion.get());

		CodePiece setCompNull = companion.set(CodePieces.constantNull());

		owner = Type.getInternalName(BitSet.class);
		name = "clone";
		desc = Type.getMethodDescriptor(getType(Object.class));
		CodePiece clone = CodePieces.castTo(BitSet.class, CodePieces.invokeVirtual(owner, name, desc, var.get()));
		CodePiece setCompNew = companion.set(clone);

		owner = Type.getInternalName(BitSet.class);
		name = "clear";
		desc = Type.getMethodDescriptor(VOID_TYPE);
		CodePiece clear = CodePieces.invokeVirtual(owner, name, desc, companion.get());

		owner = Type.getInternalName(BitSet.class);
		name = "or";
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(BitSet.class));
		CodePiece copy = CodePieces.invokeVirtual(owner, name, desc, companion.get(), var.get());

		CodePiece update = varNull.doIfElse(setCompNull, compNull.doIfElse(setCompNew, clear.append(copy)));
		return write.append(update);
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readBitSet";
		String desc = Type.getMethodDescriptor(getType(BitSet.class), getType(BitSet.class));
		return var.set(CodePieces.invokeVirtual(owner, name, desc, stream, var.get()));
	}
}
