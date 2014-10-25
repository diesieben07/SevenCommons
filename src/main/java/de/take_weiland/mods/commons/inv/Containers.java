package de.take_weiland.mods.commons.inv;

import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.util.ItemStacks;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Containers {

	public static final int PLAYER_INV_Y_DEFAULT = 84;
	public static final int PLAYER_INV_X_DEFAULT = 8;

	public static void addPlayerInventory(Container container, InventoryPlayer inventoryPlayer) {
		addPlayerInventory(container, inventoryPlayer, PLAYER_INV_X_DEFAULT, PLAYER_INV_Y_DEFAULT);
	}

	public static void addPlayerInventory(Container container, InventoryPlayer inventoryPlayer, int xStart, int yStart) {
		Set<UUID> allItemInvs = allItemInventories(container);

		// add the upper 3 rows
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 9; i++) {
				SCReflector.instance.addSlot(container, new PlayerSlot(inventoryPlayer, i + j * 9 + 9, xStart + i * 18, yStart + j * 18, allItemInvs));
			}
		}
		// add the hotbar
		for (int k = 0; k < 9; k++) {
			SCReflector.instance.addSlot(container, new PlayerSlot(inventoryPlayer, k, xStart + k * 18, yStart + 58, allItemInvs));
		}
	}

	public static ItemStack handleShiftClick(Container container, EntityPlayer player, int slotIndex) {
		@SuppressWarnings("unchecked")
		List<Slot> slots = container.inventorySlots;
		Slot sourceSlot = slots.get(slotIndex);
		ItemStack inputStack = sourceSlot.getStack();
		if (inputStack == null) return null;

		IInventory sourceInv = sourceSlot.inventory;
		boolean sourceIsPlayer = sourceInv == player.inventory;

		if (sourceIsPlayer) {
			// transfer to any inventory
			if (mergeStack(player.inventory, false, sourceSlot, slots, false)) {
				return null;
			} else {
				return inputStack;
			}
		} else {
			// transfer to player inventory
			// this is heuristic, but should do fine. if it doesn't the only "issue" is that vanilla behavior is not matched 100%
			boolean isMachineOutput = !sourceSlot.isItemValid(inputStack);
			if (mergeStack(player.inventory, true, sourceSlot, slots, !isMachineOutput)) {
				return null;
			} else {
				return inputStack;
			}
		}
	}

	// returns true on a successful full merge
	private static boolean mergeStack(InventoryPlayer playerInv, boolean mergeIntoPlayer, Slot sourceSlot, List<Slot> slots, boolean reverse) {
		ItemStack sourceStack = sourceSlot.getStack();

		int originalSize = sourceStack.stackSize;

		int len = slots.size();
		int idx;

		// first pass, try to merge with existing stacks
		// can skip this if stack is not stackable at all
		if (sourceStack.isStackable()) {
			idx = reverse ? len - 1 : 0;

			while (sourceStack.stackSize > 0 && (reverse ? idx >= 0 : idx < len)) {
				Slot targetSlot = slots.get(idx);
				if (mergeIntoPlayer ? targetSlot.inventory == playerInv : targetSlot.inventory != playerInv) {
					ItemStack target = targetSlot.getStack();
					if (ItemStacks.equal(sourceStack, target)) { // also checks target != null, because stack is never null
						int targetMax = Math.min(targetSlot.getSlotStackLimit(), target.getMaxStackSize());
						int toTransfer = Math.min(sourceStack.stackSize, targetMax - target.stackSize);
						if (toTransfer > 0) {
							target.stackSize += toTransfer;
							sourceStack.stackSize -= toTransfer;
							targetSlot.onSlotChanged();
						}
					}
				}

				if (reverse) {
					idx--;
				} else {
					idx++;
				}
			}
			if (sourceStack.stackSize == 0) {
				sourceSlot.putStack(null);
				return true;
			}
		}

		// 2nd pass: try to put anything remaining into a free slot
		idx = reverse ? len - 1 : 0;
		while (reverse ? idx >= 0 : idx < len) {
			Slot targetSlot = slots.get(idx);
			if (!targetSlot.getHasStack() && targetSlot.isItemValid(sourceStack)) {
				targetSlot.putStack(sourceStack);
				sourceSlot.putStack(null);
				return true;
			}

			if (reverse) {
				idx--;
			} else {
				idx++;
			}
		}

		// we had success in merging only a partial stack
		if (sourceStack.stackSize != originalSize) {
			sourceSlot.onSlotChanged();
		}
		return false;
	}

	private static Set<UUID> allItemInventories(Container container) {
		ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
		ItemInventory last = null;

		@SuppressWarnings("unchecked")
		List<Slot> slots = container.inventorySlots;
		for (Slot slot : slots) {
			// keep reference to last found inventory, to avoid adding
			// many duplicates to the builder, because the filtering of those
			// does not happen before .build()
			if (last != slot.inventory && slot.inventory instanceof ItemInventory) {
				last = (ItemInventory) slot.inventory;
				builder.add(last.uuid);
			}
		}
		return builder.build();
	}

	private Containers() { }
}
