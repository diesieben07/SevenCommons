package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * @author diesieben07
 */
class PrimitiveBoxHandler extends PropertyHandler {

	private final Type unboxed;
	private ASMVariable companion;

	PrimitiveBoxHandler(ASMVariable var, int idx, Type unboxed) {
		super(var, idx);
		this.unboxed = unboxed;
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifEqual(var.get(), companion.get(), var.getType()).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "write" + StringUtils.capitalize(unboxed.getClassName()) + "Box";
		String desc = Type.getMethodDescriptor(VOID_TYPE, var.getType());
		return CodePieces.invokeVirtual(owner, name, desc, stream, var.get()).append(companion.set(var.get()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "read" + StringUtils.capitalize(unboxed.getClassName()) + "Box";
		String desc = Type.getMethodDescriptor(var.getType());
		return var.set(CodePieces.invokeVirtual(owner, name, desc, stream));
	}
}
