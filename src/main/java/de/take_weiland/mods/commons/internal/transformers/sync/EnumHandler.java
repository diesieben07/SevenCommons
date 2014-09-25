package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;
import static scala.tools.asm.Opcodes.INVOKEVIRTUAL;

/**
 * @author diesieben07
 */
class EnumHandler extends PropertyHandler {

	private ASMVariable companion;

	EnumHandler(ASMVariable var, int idx) {
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
		String name = "writeEnum";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Enum.class));
		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get()).append(companion.set(var.get()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readEnum";
		String desc = Type.getMethodDescriptor(getType(Enum.class), getType(Class.class));
		CodePiece read = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, CodePieces.constant(var.getType()));

		return var.set(CodePieces.castTo(var.getType(), read));
	}
}
