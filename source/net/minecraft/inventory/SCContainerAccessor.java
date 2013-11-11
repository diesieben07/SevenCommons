package net.minecraft.inventory;

import net.minecraft.item.ItemStack;

public final class SCContainerAccessor {

	private SCContainerAccessor() { }
	
	public static void addSlot(Container container, Slot slot) {
		container.addSlotToContainer(slot);
	}
	
	public static boolean mergeItemStack(Container container, ItemStack stack, int slotStart, int slotEnd, boolean direction) {
		return container.mergeItemStack(stack, slotStart, slotEnd, direction);
	}
	
}
