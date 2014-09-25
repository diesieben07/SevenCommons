package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
class StringHandler extends PropertyHandler {

	private ASMVariable companion;

	StringHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		String name = SyncTransformer.companionName(var);
		String desc = Type.getDescriptor(String.class);
		FieldNode field = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		state.clazz.fields.add(field);
		companion = ASMVariables.of(state.clazz, field, CodePieces.getThis());
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifEqual(var.get(), companion.get(), Type.getType(String.class)).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "writeString";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(String.class));
		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get()).append(companion.set(var.get()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readString";
		String desc = Type.getMethodDescriptor(getType(String.class));
		return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream));
	}
}
