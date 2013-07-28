package de.take_weiland.mods.commons.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
	
	public static final NBTTagList writeInventory(IInventory inventory) {
		NBTTagList nbt = new NBTTagList();

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack item = inventory.getStackInSlot(i);
			NBTTagCompound itemCompound = item.writeToNBT(new NBTTagCompound());
			itemCompound.setShort("slot", UnsignedShorts.checkedCast(i));
			nbt.appendTag(itemCompound);
		}
		return nbt;
	}
	
	public static final void readInventory(IInventory inventory, NBTTagList nbtList) {
		for (NBTTagCompound nbt : ModdingUtils.<NBTTagCompound>iterate(nbtList)) {
			ItemStack item = ItemStack.loadItemStackFromNBT(nbt);
			inventory.setInventorySlotContents(UnsignedShorts.toInt(nbt.getShort("slot")), item);
		}
	}
	
	public static final Iterable<ItemStack> iterate(final IInventory inventory) {
		return new Iterable<ItemStack>() {
			
			@Override
			public Iterator<ItemStack> iterator() {
				return new Iterator<ItemStack>() {

					private int next = 0;
					
					@Override
					public boolean hasNext() {
						return next < inventory.getSizeInventory();
					}

					@Override
					public ItemStack next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						return inventory.getStackInSlot(next++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Can't modify Inventory size!");
					}
				};
			}
		};
	}
	
}
