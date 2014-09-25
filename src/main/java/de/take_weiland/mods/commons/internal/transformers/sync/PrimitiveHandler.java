package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;

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
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "write" + StringUtils.capitalize(var.getType().getClassName());
		String desc = Type.getMethodDescriptor(VOID_TYPE, var.getType());
		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get())
				.append(companion.set(var.get()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "read" + StringUtils.capitalize(var.getType().getClassName());
		String desc = Type.getMethodDescriptor(var.getType());
		return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream));
	}
}
