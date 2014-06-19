package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Type;

/**
* @author diesieben07
*/
class ItemStackSyncer extends Syncer {

	private static ItemStackSyncer instance;

	static ItemStackSyncer instance() {
		return instance == null ? (instance = new ItemStackSyncer()) : instance;
	}

	@Override
	ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
		return Conditions.ifTrue(CodePieces.invokeStatic(Type.getInternalName(ItemStacks.class),
				"equal", ASMUtils.getMethodDescriptor(boolean.class, ItemStack.class, ItemStack.class),
				oldValue, newValue));
	}

	@Override
	CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
		return CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
				"writeItemStack", ASMUtils.getMethodDescriptor(void.class, WritableDataBuf.class, ItemStack.class),
				packetBuilder, newValue);
	}

	@Override
	CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
		return CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
				"readItemStack", ASMUtils.getMethodDescriptor(ItemStack.class, DataBuf.class),
				packetBuilder);
	}
}
