package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.block.Block;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
class BlockHandler extends PropertyHandler {

	private ASMVariable companion;

	BlockHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifSame(var.get(), companion.get(), Type.getType(Block.class)).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "writeBlock";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Block.class));
		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get()).append(companion.set(var.get()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readBlock";
		String desc = Type.getMethodDescriptor(getType(Block.class));
		CodePiece read = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream);

		boolean isPureBlock = var.getType().getInternalName().equals("net/minecraft/block/Block");
		return var.set(isPureBlock ? read : CodePieces.castTo(var.getType(), read));
	}
}
