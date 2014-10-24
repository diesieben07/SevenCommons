package de.take_weiland.mods.commons.inv;

import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.util.ItemStacks;
import de.take_weiland.mods.commons.util.JavaUtils;
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

	public static ItemStack shiftClickImpl(Container container, EntityPlayer player, int slotIndex) {
		@SuppressWarnings("unchecked")
		List<Slot> slots = container.inventorySlots;
		Slot sourceSlot = slots.get(slotIndex);
		ItemStack inputStack = sourceSlot.getStack();
		if (inputStack == null) return null;

		IInventory sourceInv = sourceSlot.inventory;
		boolean sourceIsPlayer = sourceInv == player.inventory;

		if (sourceIsPlayer) {
			// transfer to any inventory
		} else {
			// transfer to player inventory
			// this is heuristic, but should do fine. if it doesn't the only "issue" is that vanilla behavior is not matched 100%
			boolean isMachineOutput = !sourceSlot.isItemValid(inputStack);
			if (isMachineOutput) {
				int playerInvStart = -1;
				int playerInvEnd = -1;
				for (int i = 0, len = slots.size(); i < len; i++) {
					Slot current = slots.get(i);
					if (playerInvStart == -1) {
						if (current.inventory == player.inventory) {
							playerInvStart = i;
						}
					} else if (playerInvEnd == -1) {
						if (current.inventory != player.inventory) {
							playerInvEnd = i;
						}
					} else {
						if (current.inventory == player.inventory) {
							throw new RuntimeException("Non-connected player-inventory in Container " + container.getClass().getName());
						}
					}
				}
			}
		}

		Slot target = null;
		for (int i = 0, len = slots.size(); i < len; i++) {
			if (i == slotIndex) continue;
			Slot candidate = slots.get(i);
			if (candidate.inventory == sourceInv || !candidate.isItemValid(inputStack)) continue;
			if (candidate.getHasStack()) {
				ItemStack targetStack = candidate.getStack();
				if (!ItemStacks.equal(targetStack, inputStack)) continue;
				if (targetStack.stackSize >= Math.min(candidate.getSlotStackLimit(), targetStack.getMaxStackSize())) {
					continue;
				}
			}
			target = candidate;
			break;
		}
		if (target == null) {
			System.out.println("Did not find a target for " + inputStack);
			return null;
		}

		ItemStack mergeInto = target.getStack();
		int numTransfer;
		if (mergeInto == null) {
			numTransfer = target.getSlotStackLimit();
		} else {
			numTransfer = Math.min(target.getSlotStackLimit(), mergeInto.getMaxStackSize()) - mergeInto.stackSize;
		}
		numTransfer = Math.min(numTransfer, inputStack.stackSize);

		System.out.println("Planning to transfer: " + numTransfer);
		System.out.println("Transferring to: " + mergeInto + " @ " + target.inventory);
		if (mergeInto == null) {
			target.putStack(inputStack.splitStack(numTransfer));
			inputStack = null;
		} else {
			inputStack.stackSize -= numTransfer;
			mergeInto.stackSize += numTransfer;
			target.onSlotChanged();
		}
		if (inputStack == null || inputStack.stackSize == 0) {
			sourceSlot.putStack(null);
		} else {
			sourceSlot.onSlotChanged();
		}
		return ItemStacks.emptyToNull(inputStack);
	}

	/**
	 * generic implementation for {@link Container#transferStackInSlot}
	 *
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
