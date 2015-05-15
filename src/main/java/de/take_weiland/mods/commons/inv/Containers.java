package de.take_weiland.mods.commons.inv;

import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ContainerProxy;
import de.take_weiland.mods.commons.internal.PacketContainerButton;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.ItemStacks;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * <p>Utilities for working with {@link net.minecraft.inventory.Container Inventory Containers}.</p>
 */
public final class Containers {

    static final int PLAYER_INV_Y_DEFAULT = 84;
    static final int PLAYER_INV_X_DEFAULT = 8;
    private static final int SLOT_HEIGHT = 18;

    /**
     * <p>Add the full player inventory to the given Container.</p>
     * <p>This method uses the default coordinates (8, 84) as the start position and each Slot is assumed to be 18x18 pixels.</p>
     *
     * @param container       the Container
     * @param inventoryPlayer the player inventory
     */
    public static void addPlayerInventory(Container container, InventoryPlayer inventoryPlayer) {
        addPlayerInventory(container, inventoryPlayer, PLAYER_INV_X_DEFAULT, PLAYER_INV_Y_DEFAULT);
    }

    /**
     * <p>Add the full player inventory to the given Container.</p>
     * <p>This method uses the given start coordinates, each Slot is assumed to be 18x18 pixels.</p>
     *
     * @param container       the Container
     * @param inventoryPlayer the player inventory
     * @param xStart          the x coordinate
     * @param yStart          the y coordinate
     */
    public static void addPlayerInventory(Container container, InventoryPlayer inventoryPlayer, int xStart, int yStart) {
        Set<UUID> allItemInvs = allItemInventories(container);

        // add the upper 3 rows
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                SCReflector.instance.addSlot(container, new PlayerSlot(inventoryPlayer, x + y * 9 + 9, xStart + x * SLOT_HEIGHT, yStart + y * SLOT_HEIGHT, allItemInvs));
            }
        }
        // add the hotbar
        for (int k = 0; k < 9; k++) {
            SCReflector.instance.addSlot(container, new PlayerSlot(inventoryPlayer, k, xStart + k * 18, yStart + 58, allItemInvs));
        }
    }

    /**
     * <p>Trigger the given button on the currently open Container. This method will call
     * {@link de.take_weiland.mods.commons.inv.ButtonContainer#onButtonClick(cpw.mods.fml.relauncher.Side, net.minecraft.entity.player.EntityPlayer, int)}
     * on both client and server.</p>
     * <p>This method must only be called from the client thread and will throw a {@code ClassCastException} if the
     * currently open container does not implement {@code ButtonContainer}.</p>
     *
     * @param button the button
     */
    public static void triggerButton(int button) {
        EntityPlayer player = SevenCommons.proxy.getClientPlayer();
        ((ButtonContainer) player.openContainer).onButtonClick(Side.CLIENT, player, button);
        new PacketContainerButton(player.openContainer.windowId, button).sendToServer();
    }

    /**
     * <p>Get all inventories present in the given Container. The inventories are returned in order of first appearance
     * by the Iterator of the returned Set.</p>
     *
     * @param container the Container
     * @return all inventories
     */
    public static ImmutableSet<IInventory> getInventories(Container container) {
        return ((ContainerProxy) container)._sc$getInventories();
    }

    /**
     * <p>Get the player interacting with the given Container. On the client this is always {@code Minecraft#thePlayer}.</p>
     *
     * @param container the Container
     * @return the player
     */
    public static EntityPlayer getViewer(Container container) {
        List<ICrafting> listeners = SCReflector.instance.getCrafters(container);
        for (int i = 0, len = listeners.size(); i < len; i++) {
            ICrafting listener = listeners.get(i);
            if (listener instanceof EntityPlayerMP) {
                return (EntityPlayer) listener;
            }
        }
        return SevenCommons.proxy.getClientPlayer();
    }

    /**
     * <p>Implementation for shift-clicking in Containers. This is a drop-in replacement you can call from the
     * {@link Container#transferStackInSlot(net.minecraft.entity.player.EntityPlayer, int)} method in your Container.</p>
     * <ul>
     * <li>When the slot to be moved is not in the inventory of the player it will be moved there as known
     * from vanilla inventories.</li>
     * <li>When the slot to be moved is in the inventory of the player it will be moved to the first available Slot
     * that accepts it.</li>
     * </ul>
     * <p>This behavior can be overridden by implementing {@link de.take_weiland.mods.commons.inv.SpecialShiftClick} on
     * your Container.</p>
     *
     * @param container the Container
     * @param player    the player performing the shift-click
     * @param slotIndex the slot being shift-clicked
     * @return null if no transfer is possible
     */
    public static ItemStack handleShiftClick(Container container, EntityPlayer player, int slotIndex) {
        @SuppressWarnings("unchecked")
        List<Slot> slots = container.inventorySlots;
        Slot sourceSlot = slots.get(slotIndex);
        ItemStack inputStack = sourceSlot.getStack();
        if (inputStack == null) {
            return null;
        }

        boolean sourceIsPlayer = sourceSlot.inventory == player.inventory;

        ItemStack copy = inputStack.copy();

        if (sourceIsPlayer) {
            if (container instanceof SpecialShiftClick) {
                ShiftClickTarget target = ((SpecialShiftClick) container).getShiftClickTarget(inputStack, player);
                if (!target.isStandard()) {
                    if (target.isNone() || !mergeToTarget(player.inventory, sourceSlot, slots, target)) {
                        return null;
                    } else {
                        return copy;
                    }
                }
            }
            // transfer to any inventory
            if (!mergeStack(player.inventory, false, sourceSlot, slots, false)) {
                return null;
            } else {
                return copy;
            }
        } else {
            // transfer to player inventory
            // this is heuristic, but should do fine. if it doesn't the only "issue" is that vanilla behavior is not matched 100%
            boolean isMachineOutput = !sourceSlot.isItemValid(inputStack);
            if (!mergeStack(player.inventory, true, sourceSlot, slots, !isMachineOutput)) {
                return null;
            } else {
                return copy;
            }
        }
    }

    // same as mergeStack, but uses a ShiftClickTarget
    // ugly copy paste is ugly
    private static boolean mergeToTarget(InventoryPlayer playerInv, Slot sourceSlot, List<Slot> slots, ShiftClickTarget target) {
        ItemStack sourceStack = sourceSlot.getStack();
        int originalSize = sourceStack.stackSize;

        // first pass, try merge with existing stacks
        target.reset();
        while (sourceStack.stackSize > 0 && target.hasNext()) {
            Slot targetSlot = slots.get(target.next());
            if (targetSlot.inventory != playerInv) {
                ItemStack targetStack = targetSlot.getStack();
                if (ItemStacks.equal(targetStack, sourceStack)) {
                    int targetMax = Math.min(targetSlot.getSlotStackLimit(), targetStack.getMaxStackSize());
                    int toTransfer = Math.min(sourceStack.stackSize, targetMax - targetStack.stackSize);
                    if (toTransfer > 0) {
                        targetStack.stackSize += toTransfer;
                        sourceStack.stackSize -= toTransfer;
                        targetSlot.onSlotChanged();
                    }
                }
            }
        }
        if (sourceStack.stackSize == 0) {
            sourceSlot.putStack(null);
            return true;
        }

        //2nd pass: merge anything leftover into empty slots
        target.reset();
        while (target.hasNext()) {
            Slot targetSlot = slots.get(target.next());
            if (targetSlot.inventory != playerInv && !targetSlot.getHasStack() && targetSlot.isItemValid(sourceStack)) {
                targetSlot.putStack(sourceStack);
                sourceSlot.putStack(null);
                return true;
            }
        }
        if (originalSize != sourceStack.stackSize) {
            sourceSlot.onSlotChanged();
            return true;
        } else {
            return false;
        }
    }

    // returns true if it has found a target
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
                if ((targetSlot.inventory == playerInv) == mergeIntoPlayer) {
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
            if ((targetSlot.inventory == playerInv) == mergeIntoPlayer
                    && !targetSlot.getHasStack() && targetSlot.isItemValid(sourceStack)) {
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
            return true;
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

    private Containers() {
    }
}
