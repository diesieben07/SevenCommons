package de.take_weiland.mods.commons.util;

import static net.minecraft.inventory.SCContainerAccessor.addSlot;
import static net.minecraft.inventory.SCContainerAccessor.mergeItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.gui.AdvancedContainer;

public final class Containers {

	private Containers() {
	}

	public static final void addPlayerInventory(Container container, InventoryPlayer inventoryPlayer) {
		addPlayerInventory(container, inventoryPlayer, 8, 84);
	}

	public static final void addPlayerInventory(Container container, InventoryPlayer inventoryPlayer, int xStart, int yStart) {
		// add the upper 3 rows
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 9; i++) {
				addSlot(container, new Slot(inventoryPlayer, i + j * 9 + 9, xStart + i * 18, yStart + j * 18));
			}
		}

		// add the hotbar
		for (int k = 0; k < 9; k++) {
			addSlot(container, new Slot(inventoryPlayer, k, xStart + k * 18, yStart + 58));
		}
	}

	public static <T extends Container & AdvancedContainer<?>> ItemStack transferStack(T container, EntityPlayer player, int slotIndex) {
		ItemStack result = null;
		
		Slot slot = (Slot) container.inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			
			ItemStack stackInSlot = slot.getStack();
			result = stackInSlot.copy();

			IInventory playerInv = container.getPlayer().inventory;
			int firstPlayerSlot = container.getFirstPlayerSlot();
			
			if (slot.inventory == playerInv) {
				int targetSlot = container.getMergeTargetSlot(stackInSlot);
				if (targetSlot >= 0) {
					if (!mergeItemStack(container, stackInSlot, targetSlot, targetSlot + 1, false)) {
						return null;
					}
				} else if (slotIndex >= firstPlayerSlot && slotIndex < firstPlayerSlot + 27) {
					if (!mergeItemStack(container, stackInSlot, firstPlayerSlot + 27, firstPlayerSlot + 36, false)) {
						return null;
					}
				} else if (slotIndex >= firstPlayerSlot + 27 && slotIndex < firstPlayerSlot + 36 && !mergeItemStack(container, stackInSlot, firstPlayerSlot, firstPlayerSlot + 27, false)) {
					return null;
				}
			} else if (!mergeItemStack(container, stackInSlot, firstPlayerSlot, firstPlayerSlot + 36, false)) {
				return null;
			}

			if (stackInSlot.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}

			if (stackInSlot.stackSize == result.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(player, stackInSlot);
		}

		return result;
	}

}
