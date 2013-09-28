package de.take_weiland.mods.commons.util;

import static net.minecraft.inventory.SCContainerAccessor.addSlot;
import static net.minecraft.inventory.SCContainerAccessor.mergeItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.internal.PacketSync;
import de.take_weiland.mods.commons.templates.AdvancedContainer;
import de.take_weiland.mods.commons.templates.ItemInventory;
import de.take_weiland.mods.commons.templates.SlotNoPickup;

public final class Containers {

	private Containers() {
	}

	public static <T extends Container & AdvancedContainer<?>> void addPlayerInventory(T container, InventoryPlayer inventoryPlayer) {
		addPlayerInventory(container, inventoryPlayer, 8, 84);
	}

	public static <T extends Container & AdvancedContainer<?>> void addPlayerInventory(T container, InventoryPlayer inventoryPlayer, int xStart, int yStart) {
		// add the upper 3 rows
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 9; i++) {
				addSlot(container, new Slot(inventoryPlayer, i + j * 9 + 9, xStart + i * 18, yStart + j * 18));
			}
		}

		IInventory inv = container.inventory();
		int blockedSlot = inv instanceof ItemInventory.WithPlayer ? ((ItemInventory.WithPlayer)inv).getHotbarIndex() : -1;
		// add the hotbar
		for (int k = 0; k < 9; k++) {
			if (k == blockedSlot) {
				addSlot(container, new SlotNoPickup(inventoryPlayer, k, xStart + k * 18, yStart + 58));
			} else {
				addSlot(container, new Slot(inventoryPlayer, k, xStart + k * 18, yStart + 58));
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

	public static void sync(AdvancedContainer<?> container) {
		if (container.isSynced() && container.getPlayer() instanceof EntityPlayerMP) {
			new PacketSync(container).sendTo(container.getPlayer());
		}
	}
}
