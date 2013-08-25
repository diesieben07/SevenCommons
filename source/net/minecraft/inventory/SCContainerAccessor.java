package net.minecraft.inventory;

import net.minecraft.item.ItemStack;

public final class SCContainerAccessor {

	private SCContainerAccessor() { }
	
	public static final void addSlot(Container container, Slot slot) {
		container.addSlotToContainer(slot);
	}
	
	public static boolean mergeItemStack(Container container, ItemStack stack, int par2, int par3, boolean par4) {
		return container.mergeItemStack(stack, par2, par3, par4);
	}
	
}
