package de.take_weiland.mods.commons.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

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
                } else {
                	inventory.onInventoryChanged();
                }
                
                return returnStack;
            }
        } else {
            return null;
        }
	}
	
	public static final ItemStack getAndRemove(IInventory inventory, int slot) {
		ItemStack item = inventory.getStackInSlot(slot);
		inventory.setInventorySlotContents(slot, null);
		return item;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T extends TileEntity & IInventory> void spillIfInventory(TileEntity te) {
		if (te instanceof IInventory) {
			spill((T)te);
		}
	}
	
	public static final <T extends TileEntity & IInventory> void spill(T tileEntity) {
		Random rand = tileEntity.worldObj.rand;
		for (ItemStack stack : iterate(tileEntity)) {
            if (stack != null) {
                float randomPositionX = rand.nextFloat() * 0.8F + 0.1F;
                float randomPositionY = rand.nextFloat() * 0.8F + 0.1F;
                float randomPositionZ = rand.nextFloat() * 0.8F + 0.1F;

                while (stack.stackSize > 0) {
                    int partialStackSize = rand.nextInt(21) + 10;

                    if (partialStackSize > stack.stackSize) {
                        partialStackSize = stack.stackSize;
                    }

                    stack.stackSize -= partialStackSize;
                    EntityItem itemEntity = new EntityItem(tileEntity.worldObj, tileEntity.xCoord + randomPositionX, tileEntity.yCoord + randomPositionY, tileEntity.zCoord + randomPositionZ, new ItemStack(stack.itemID, partialStackSize, stack.getItemDamage()));

                    if (stack.hasTagCompound()) {
                        itemEntity.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
                    }

                    float motionMultiplier = 0.05F;
                    itemEntity.motionX = rand.nextGaussian() * motionMultiplier;
                    itemEntity.motionY = rand.nextGaussian() * motionMultiplier + 0.2F;
                    itemEntity.motionZ = rand.nextGaussian() * motionMultiplier;
                    tileEntity.worldObj.spawnEntityInWorld(itemEntity);
                }
            }
        }
	}
	
	public static final NBTTagList writeInventory(IInventory inventory) {
		NBTTagList nbt = new NBTTagList();

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null) {
				NBTTagCompound itemCompound = item.writeToNBT(new NBTTagCompound());
				itemCompound.setShort("slot", UnsignedShorts.checkedCast(i));
				nbt.appendTag(itemCompound);
			}
		}
		return nbt;
	}
	
	public static final void readInventory(IInventory inventory, NBTTagList nbtList) {
		for (NBTTagCompound nbt : ModdingUtils.<NBTTagCompound>iterate(nbtList)) {
			ItemStack item = ItemStack.loadItemStackFromNBT(nbt);
			inventory.setInventorySlotContents(UnsignedShorts.toInt(nbt.getShort("slot")), item);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final Iterator<ItemStack> iterator(final IInventory inventory) {
		if (inventory instanceof Iterable) {
			return ((Iterable<ItemStack>)inventory).iterator();
		}
		
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
	
	@SuppressWarnings("unchecked")
	public static final Iterable<ItemStack> iterate(final IInventory inventory) {
		if (inventory instanceof Iterable) {
			return (Iterable<ItemStack>)inventory;
		}
		return new Iterable<ItemStack>() {
			
			@Override
			public Iterator<ItemStack> iterator() {
				return InventoryUtils.iterator(inventory);
			}
		};
	}
	
}
