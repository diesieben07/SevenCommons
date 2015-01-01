package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.apache.commons.lang3.StringUtils;

/**
* @author diesieben07
*/
class PrimitiveHandler extends PropertyHandler {

	private ASMVariable companion;

	PrimitiveHandler(ASMVariable var, int idx, TransformState state) {
		super(var, idx, state);
	}

	@Override
	void initialTransform() {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.isSame(var.get(), companion.get(), var.getType()).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		return write(stream)
				.append(companion.set(var.get()));
	}

	@Override
	CodePiece write(CodePiece stream) {
		String methodName = "write" + StringUtils.capitalize(var.getType().getClassName());
		return CodePieces.invokeVirtual(MCDataOutputStream.class, methodName, stream, void.class,
				var.getType(), var.get());
	}

	@Override
	CodePiece read(CodePiece stream) {
		String name = "read" + StringUtils.capitalize(var.getType().getClassName());
		return var.set(CodePieces.invokeVirtual(MCDataInputStream.class, name, stream, var.getType()));
	}

}
