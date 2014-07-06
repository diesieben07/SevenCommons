package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public final class Containers {

	public static final int PLAYER_INV_Y_DEFAULT = 84;
	public static final int PLAYER_INV_X_DEFAULT = 8;

	private Containers() {
	}

	public static <T extends Container & SCContainer<?>> void addPlayerInventory(T container, InventoryPlayer inventoryPlayer) {
		addPlayerInventory(container, inventoryPlayer, PLAYER_INV_X_DEFAULT, PLAYER_INV_Y_DEFAULT);
	}

	public static <T extends Container & SCContainer<?>> void addPlayerInventory(T container, InventoryPlayer inventoryPlayer, int xStart, int yStart) {
		// add the upper 3 rows
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 9; i++) {
				SCReflector.instance.addSlot(container, new Slot(inventoryPlayer, i + j * 9 + 9, xStart + i * 18, yStart + j * 18));
			}
		}

		IInventory inv = container.inventory();
		int blockedSlot;
		if (inv instanceof ItemInventory.WithInventory) {
			ItemInventory.WithInventory<?> iinv = (ItemInventory.WithInventory<?>) inv;
			blockedSlot = iinv.inv == inventoryPlayer ? iinv.slot : -1;
		} else {
			blockedSlot = -1;
		}
		// add the hotbar
		for (int k = 0; k < 9; k++) {
			if (k == blockedSlot) {
				SCReflector.instance.addSlot(container, new SlotNoPickup(inventoryPlayer, k, xStart + k * 18, yStart + 58));
			} else {
				SCReflector.instance.addSlot(container, new Slot(inventoryPlayer, k, xStart + k * 18, yStart + 58));
			}
		}
	}

	/**
	 * generic implementation for {@link Container#transferStackInSlot}
	 * @param container
	 * @param player
	 * @param slotIndex
	 * @return
	 */
	public static <T extends Container & SCContainer<?>> ItemStack transferStack(T container, EntityPlayer player, int slotIndex) {
		ItemStack result = null;
		
		Slot slot = (Slot) container.inventorySlots.get(slotIndex);
		
		int firstPlayerSlot = container.getFirstPlayerSlot();
		if (firstPlayerSlot < 0) {
			return null;
		}

		if (slot != null && slot.getHasStack()) {
			
			ItemStack stackInSlot = slot.getStack();
			result = stackInSlot.copy();

			IInventory playerInv = container.getPlayer().inventory;
			
			if (slot.inventory == playerInv) {
				long enc = container.getSlotRange(stackInSlot);
				int targetBegin = JavaUtils.decodeIntA(enc);
				int targetEnd = JavaUtils.decodeIntB(enc);
				if (targetBegin != -1) {
					if (!SCReflector.instance.mergeItemStack(container, stackInSlot, targetBegin, targetEnd, false)) {
						return null;
					}
				} else if (slotIndex >= firstPlayerSlot && slotIndex < firstPlayerSlot + 27) {
					if (!SCReflector.instance.mergeItemStack(container, stackInSlot, firstPlayerSlot + 27, firstPlayerSlot + 36, false)) {
						return null;
					}
				} else if (slotIndex >= firstPlayerSlot + 27 && slotIndex < firstPlayerSlot + 36 && !SCReflector.instance.mergeItemStack(container, stackInSlot, firstPlayerSlot, firstPlayerSlot + 27, false)) {
					return null;
				}
			} else if (!SCReflector.instance.mergeItemStack(container, stackInSlot, firstPlayerSlot, firstPlayerSlot + 36, false)) {
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
