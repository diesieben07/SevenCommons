package de.take_weiland.mods.commons.internal.client.worldview;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public class ViewEntity extends EntityLivingBase {

    public ViewEntity(World world) {
        super(world);
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public ItemStack getHeldItem() {
        return null;
    }

    @Override
    public ItemStack getEquipmentInSlot(int slot) {
        return null;
    }

    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack stack) {

    }

    @Override
    public ItemStack[] getInventory() {
        return new ItemStack[0];
    }
}
