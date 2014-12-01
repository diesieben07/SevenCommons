package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Sync;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

/**
* @author diesieben07
*/
class PrimitiveHandler extends PropertyHandler {

	private ASMVariable companion;

	PrimitiveHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifSame(var.get(), companion.get(), var.getType()).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String name = "write" + StringUtils.capitalize(var.getType().getClassName());
		return CodePieces.invokeVirtual(MCDataOutputStream.class, name, stream, void.class, var.getType(), var.get())
				.append(companion.set(var.get()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		String name = "read" + StringUtils.capitalize(var.getType().getClassName());
		return var.set(CodePieces.invokeVirtual(MCDataInputStream.class, name, stream, var.getType()));
	}

	@Override
	Class<? extends Annotation> getAnnotation() {
		return Sync.class;
	}
}
