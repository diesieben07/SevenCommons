package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

/**
 * @author diesieben07
 */
class PrimitiveBoxHandler extends PropertyHandler {

	private final Type unboxed;
	private ASMVariable companion;

	PrimitiveBoxHandler(ASMVariable var, int idx, Type unboxed, TransformState state) {
		super(var, idx, state);
		this.unboxed = unboxed;
	}

	@Override
	void initialTransform() {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifEqual(var.get(), companion.get(), var.getType()).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		return write(stream)
				.append(companion.set(var.get()));
	}

	@Override
	CodePiece write(CodePiece stream) {
		String methodName = StringUtils.capitalize(unboxed.getClassName() + "Box");
		return CodePieces.invokeVirtual(MCDataOutputStream.class, methodName, stream, void.class,
				var.getType(), var.get());
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "read" + StringUtils.capitalize(unboxed.getClassName()) + "Box";
		String desc = Type.getMethodDescriptor(var.getType());
		return var.set(CodePieces.invokeVirtual(owner, name, desc, stream));
	}

}
