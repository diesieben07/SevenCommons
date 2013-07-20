package de.take_weiland.mods.commons.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public final class InventoryUtils {

	private InventoryUtils() { }
	
	public static final ItemStack decreaseStackSize(IInventory inventory, int slot, int count) {
		ItemStack stack = inventory.getStackInSlot(slot);

		if (stack != null) {
            ItemStack returnStack;

            if (stack.stackSize <= count) {
                returnStack = stack;
                inventory.setInventorySlotContents(slot, null);
                return returnStack;
            } else {
                returnStack = stack.splitStack(count);

                if (stack.stackSize == 0) {
                	inventory.setInventorySlotContents(slot, null);
                }
                
                return returnStack;
            }
        } else {
            return null;
        }
	}
	
	public static final <T extends TileEntity & IInventory> void spillContents(T tileEntity) {
		for (int slot = 0; slot < tileEntity.getSizeInventory(); slot++) {
            ItemStack stack = tileEntity.getStackInSlot(slot);

            if (stack != null) {
                float randomPositionX = tileEntity.worldObj.rand.nextFloat() * 0.8F + 0.1F;
                float randomPositionY = tileEntity.worldObj.rand.nextFloat() * 0.8F + 0.1F;
                float randomPositionZ = tileEntity.worldObj.rand.nextFloat() * 0.8F + 0.1F;

                while (stack.stackSize > 0) {
                    int partialStackSize = tileEntity.worldObj.rand.nextInt(21) + 10;

                    if (partialStackSize > stack.stackSize) {
                        partialStackSize = stack.stackSize;
                    }

                    stack.stackSize -= partialStackSize;
                    EntityItem itemEntity = new EntityItem(tileEntity.worldObj, tileEntity.xCoord + randomPositionX, tileEntity.yCoord + randomPositionY, tileEntity.zCoord + randomPositionZ, new ItemStack(stack.itemID, partialStackSize, stack.getItemDamage()));

                    if (stack.hasTagCompound()) {
                        itemEntity.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
                    }

                    float motionMultiplier = 0.05F;
                    itemEntity.motionX = tileEntity.worldObj.rand.nextGaussian() * motionMultiplier;
                    itemEntity.motionY = tileEntity.worldObj.rand.nextGaussian() * motionMultiplier + 0.2F;
                    itemEntity.motionZ = tileEntity.worldObj.rand.nextGaussian() * motionMultiplier;
                    tileEntity.worldObj.spawnEntityInWorld(itemEntity);
                }
            }
        }
	}
}
