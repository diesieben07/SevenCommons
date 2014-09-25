package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
class ItemStackHandler extends PropertyHandler {

	private ASMVariable companion;

	ItemStackHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		String owner = Type.getInternalName(ItemStacks.class);
		String name = "identical";
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(ItemStack.class), getType(ItemStack.class));
		return ASMCondition.ifFalse(CodePieces.invokeStatic(owner, name, desc, var.get(), companion.get()));
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "writeItemStack";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(ItemStack.class));
		CodePiece write = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());

		owner = Type.getInternalName(ItemStacks.class);
		name = "clone";
		desc = Type.getMethodDescriptor(getType(ItemStack.class), getType(ItemStack.class));
		return write.append(companion.set(CodePieces.invokeStatic(owner, name, desc, var.get())));
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readItemStack";
		String desc = Type.getMethodDescriptor(getType(ItemStack.class));
		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream);
	}
}
