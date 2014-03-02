package de.take_weiland.mods.commons.util;

import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import de.take_weiland.mods.commons.Listenables;
import de.take_weiland.mods.commons.templates.SCInventory;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.Random;

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
	public static ItemStack decreaseStackSize(SCInventory<?> inventory, int slot, int count) {
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
	                Listenables.onChange(inventory);
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
	public static ItemStack getAndRemove(IInventory inventory, int slot) {
		ItemStack item = inventory.getStackInSlot(slot);
		inventory.setInventorySlotContents(slot, null);
		return item;
	}
	
	/**
	 * calls {@link #spill(net.minecraft.tileentity.TileEntity)} only if the given TileEntity implements {@link IInventory}
	 * @param te the TileEntity
	 */
	public static void spillIfInventory(TileEntity te) {
		if (te instanceof IInventory) {
			spill(te.worldObj, te.xCoord, te.yCoord, te.zCoord, (IInventory) te);
		}
	}
	
	/**
	 * spill the contents of a {@link TileEntity} that also implements {@link IInventory} into the world.<br>
	 * Usually used in {@link Block#breakBlock}
	 * @param te the TileEntity
	 */
	public static <T extends TileEntity & IInventory> void spill(T te) {
		spill(te.worldObj, te.xCoord, te.yCoord, te.zCoord, te);
	}
	
	/**
	 * spill the contents of the given Inventory at the given coordinates
	 * @param world the world to spill in
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @param inventory the inventory to spill
	 */
	public static void spill(World world, int x, int y, int z, IInventory inventory) {
		if (Sides.logical(world).isServer()) {
			Random rand = world.rand;
			for (ItemStack stack : iterate(inventory, false)) {
				float randomPositionX = rand.nextFloat() * 0.8F + 0.1F;
				float randomPositionY = rand.nextFloat() * 0.8F + 0.1F;
				float randomPositionZ = rand.nextFloat() * 0.8F + 0.1F;
				
				while (stack.stackSize > 0) {
					int partialStackSize = rand.nextInt(21) + 10;
	
					if (partialStackSize > stack.stackSize) {
						partialStackSize = stack.stackSize;
					}

					stack.stackSize -= partialStackSize;
					EntityItem itemEntity = new EntityItem(world, x + randomPositionX, y + randomPositionY, z + randomPositionZ, new ItemStack(stack.itemID, partialStackSize, stack.getItemDamage()));
	
					if (stack.hasTagCompound()) {
						itemEntity.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
					}
	
					float motionMultiplier = 0.05F;
					itemEntity.motionX = rand.nextGaussian() * motionMultiplier;
					itemEntity.motionY = rand.nextGaussian() * motionMultiplier + 0.2F;
					itemEntity.motionZ = rand.nextGaussian() * motionMultiplier;
					world.spawnEntityInWorld(itemEntity);
				}
			}
		}
	}
	
	/**
	 * Serialize an {@link IInventory} to a {@link NBTTagList}<br>
	 * The contents may be read back with {@link #readInventory}
	 * @param stacks the ItemStacks to write
	 * @return the NBTTagList containing the serialized contents of the inventory
	 */
	public static NBTTagList writeInventory(ItemStack[] stacks) {
		NBTTagList nbt = new NBTTagList();
		int len = stacks.length;
		for (int i = 0; i < len; i++) {
			ItemStack item = stacks[i];
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
	 * @param stacks the array to be filled
	 * @param nbtList the NBTTagList containing the serialized contents
	 */
	public static void readInventory(ItemStack[] stacks, NBTTagList nbtList) {
		for (NBTTagCompound nbt : NBT.<NBTTagCompound>asList(nbtList)) {
			ItemStack item = ItemStack.loadItemStackFromNBT(nbt);
			stacks[UnsignedShorts.toInt(nbt.getShort("slot"))] = item;
		}
	}
	
	/**
	 * Utility method, equal to {@link Inventories#iterator(IInventory, boolean) Inventories.iterator(inventory, true)}
	 */
	public static Iterator<ItemStack> iterator(final IInventory inventory) {
		return iterator(inventory, true);
	}
	
	/**
	 * Generate an Iterator for the given {@link IInventory}
	 * @param inventory the inventory to iterate
	 * @param includeNulls if empty ItemStacks should be included in the iterator
	 * @return an Iterator
	 */
	public static Iterator<ItemStack> iterator(final IInventory inventory, boolean includeNulls) {
		Iterator<ItemStack> it =  new AbstractIterator<ItemStack>() {

			private int next = 0;
			
			@Override
			protected ItemStack computeNext() {
				return next < inventory.getSizeInventory() ? inventory.getStackInSlot(next++) : endOfData();
			}

		};
		return includeNulls ? it : Iterators.filter(it, Predicates.notNull());
	}
	
	/**
	 * Utility method, equal to {@link Inventories#iterate(IInventory, boolean) Inventories.iterate(inventory, true)}
	 */
	public static Iterable<ItemStack> iterate(IInventory inventory) {
		return iterate(inventory, true);
	}
	
	/**
	 * Generate an {@link Iterable} that calls {@link Inventories#iterator(IInventory, boolean) Inventories.iterator}
	 */
	public static Iterable<ItemStack> iterate(final IInventory inventory, final boolean includeNulls) {
		return new Iterable<ItemStack>() {
				
			@Override
			public Iterator<ItemStack> iterator() {
				return Inventories.iterator(inventory, includeNulls);
			}
		};
	}
	
}
