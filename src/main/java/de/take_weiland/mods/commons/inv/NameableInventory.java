package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

/**
 * <p>An inventory which can be named.</p>
 * <p>If a {@code NameableInventory} is used in a Container it's name will be automatically synchronized with the client.</p>
 * <p>If you implement this on a TileEntity, it will automatically take the name of a renamed ItemStack if placed, override
 * {@link #takeItemStackName(EntityPlayer, ItemStack)} to control that behavior.
 * Keep in mind that this only applies if you either register your block using
 * {@link de.take_weiland.mods.commons.util.Blocks#init(Block, String) Blocks.init} or you use
 * {@link de.take_weiland.mods.commons.templates.SCItemBlock SCItemBlock} or a subclass as your ItemBlock class.</p>
 *
 * <p>This implementation implements {@link IInventory#hasCustomInventoryName()} and {@link IInventory#getInventoryName()}
 * to refer to the abstract methods in this class appropriately.</p>
 */
public interface NameableInventory extends IInventory {

    /**
     * <p>True if this inventory has a custom name.</p>
     *
     * @return true if this inventory has a custom name
     */
    boolean hasCustomName();

    /**
     * <p>Set a custom name for this inventory.</p>
     *
     * @param name the custom name
     */
    void setCustomName(String name);

    /**
     * <p>Get the custom name for this inventory.</p>
     *
     * @return the custom name or null if none
     */
    String getCustomName();

    /**
     * <p>Get the default name for this inventory if it was not renamed. This should be an unlocalized name.</p>
     * @return the unlocalized default name
     */
    String getDefaultName();

    /**
     * <p>Whether this inventory should take the name of a renamed ItemStack when created by a player.
     * This should be called when e.g. a TileEntity implementing this interface is placed or an Entity is spawned, etc.</p>
     *
     * @param player the player
     * @param stack  the ItemStack
     * @return true to take the name
     */
    default boolean takeItemStackName(EntityPlayer player, ItemStack stack) {
        return true;
    }

    @Override
    default String getInventoryName() {
        return hasCustomName() ? getCustomName() : getDefaultName();
    }

    @Override
    default boolean isCustomInventoryName() {
        return hasCustomName();
    }

    default void writeToNBT(NBTTagCompound nbt) {
        String customName = getCustomName();
        if (customName != null) {
            nbt.setString(Inventories.CUSTOM_NAME_KEY, customName);
        }
    }

    default void readFromNBT(NBTTagCompound nbt) {
        NBTBase tag = nbt.getTag(Inventories.CUSTOM_NAME_KEY);
        if (tag != null && tag.getId() == NBT.TAG_STRING) {
            setCustomName(((NBTTagString) tag).getString());
        }
    }
}
