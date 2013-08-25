package de.take_weiland.mods.commons.util;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import com.google.common.collect.AbstractIterator;

/**
 * A collection of static utility methods regarding implementors of {@link IInventory}
 * @author diesieben07
 *
 */
public final class Inventories {

	private Inventories() { }
	
	/**
	 * generic implementation for {@link IInventory#decrStackSize}
	 * @param inventory the inventory
	 * @param slot the slot to be decreased in size
	 * @param count the number of items to be depleted
	 * @return the stack being depleted from the inventory
	 */
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
	
	/**
	 * generic implementation for {@link IInventory#getStackInSlotOnClosing}<br>
	 * gets the contents from the given slot and then empties it
	 * @param inventory the inventory
	 * @param slot the slot to get the contents from
	 * @return the slots contents
	 */
	public static final ItemStack getAndRemove(IInventory inventory, int slot) {
		ItemStack item = inventory.getStackInSlot(slot);
		inventory.setInventorySlotContents(slot, null);
		return item;
	}
	
	/**
	 * calls {@link #spill} only if the given TileEntity implements {@link IInventory}
	 * @param tileEntity the TileEntity
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends TileEntity & IInventory> void spillIfInventory(TileEntity tileEntity) {
		if (tileEntity instanceof IInventory) {
			spill((T)tileEntity);
		}
	}
	
	/**
	 * spill the contents of a {@link TileEntity} that also implements {@link IInventory} into the world.<br>
	 * Usually used in {@link Block#breakBlock}
	 * @param tileEntity the TileEntity
	 */
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
	
	/**
	 * Serialize an {@link IInventory} to a {@link NBTTagList}<br>
	 * The contents may be read back with {@link #readInventory}
	 * @param inventory the inventory to write
	 * @return the NBTTagList containing the serialized contents of the inventory
	 */
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
	
	/**
	 * Unserialize an {@link IInventory} from a {@link NBTTagList}<br>
	 * The format of the NBT should match that produced by {@link #writeInventory}
	 * @param inventory the inventory to be unserialized
	 * @param nbtList the NBTTagList containing the serialized contents
	 */
	public static final void readInventory(IInventory inventory, NBTTagList nbtList) {
		for (NBTTagCompound nbt : NBT.<NBTTagCompound>asList(nbtList)) {
			ItemStack item = ItemStack.loadItemStackFromNBT(nbt);
			inventory.setInventorySlotContents(UnsignedShorts.toInt(nbt.getShort("slot")), item);
		}
	}
	
	/**
	 * Generate an Iterator for the given {@link IInventory}
	 * @param inventory
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final Iterator<ItemStack> iterator(final IInventory inventory) {
		if (inventory instanceof Iterable) {
			return ((Iterable<ItemStack>)inventory).iterator();
		} else {
			return newIterator(inventory);
		}
	}
	
	static final Iterator<ItemStack> newIterator(final IInventory inventory) {
		return new AbstractIterator<ItemStack>() {

			private int next = 0;
			
			@Override
			protected ItemStack computeNext() {
				return next == inventory.getSizeInventory() ? endOfData() : inventory.getStackInSlot(++next);
			}
		};
	}
	
	/**
	 * Generate an {@link Iterable} for the given {@link IInventory}
	 * @param inventory the Inventory
	 * @return an Iterable that returns the stacks in the inventory in order
	 */
	@SuppressWarnings("unchecked")
	public static final Iterable<ItemStack> iterate(final IInventory inventory) {
		if (inventory instanceof Iterable) {
			return (Iterable<ItemStack>)inventory;
		}
		return new Iterable<ItemStack>() {
			
			@Override
			public Iterator<ItemStack> iterator() {
				return Inventories.newIterator(inventory);
			}
		};
	}
	
}
