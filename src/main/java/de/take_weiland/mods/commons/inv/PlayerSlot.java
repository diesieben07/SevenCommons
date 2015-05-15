package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.nbt.NBTData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Set;
import java.util.UUID;

/**
 * @author diesieben07
 */
final class PlayerSlot extends Slot {

    private final Set<UUID> itemInvUUIDs;

    PlayerSlot(IInventory playerInv, int slot, int x, int y, Set<UUID> itemInvUUIDs) {
        super(playerInv, slot, x, y);
        this.itemInvUUIDs = itemInvUUIDs;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack stack = getStack();
        if (stack == null || stack.stackTagCompound == null) {
            return true;
        }
        UUID stackUUID = NBTData.readUUID(stack.stackTagCompound.getTag(ItemInventory.NBT_UUID_KEY));
        return stackUUID == null || itemInvUUIDs.contains(stackUUID);
    }
}
