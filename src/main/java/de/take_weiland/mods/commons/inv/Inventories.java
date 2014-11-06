package de.take_weiland.mods.commons.inv;

import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.client.I18n;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.util.MiscUtil;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.util.Sides;
import de.take_weiland.mods.commons.util.UnsignedShorts;
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
import java.util.logging.Logger;

/**
 * A collection of static utility methods regarding implementors of {@link IInventory}
 *
 * @author diesieben07
 */
public final class Inventories {

	private Inventories() { }

	private static final Logger logger = MiscUtil.getLogger("SC|Inventories");

	/**
	 * generic implementation for {@link IInventory#decrStackSize}
	 *
	 * @param inventory the inventory
	 * @param slot      the slot to be decreased in size
	 * @param count     the number of items to be depleted
	 * @return the stack being depleted from the inventory
	 */
	public static ItemStack decreaseStackSize(IInventory inventory, int slot, int count) {
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

	@SideOnly(Side.CLIENT)
	public static String getDisplayName(IInventory inv) {
		return inv.isInvNameLocalized() ? inv.getInvName() : I18n.translate(inv.getInvName());
	}

	/**
	 * alias for {@link #getAndRemove(net.minecraft.inventory.IInventory, int)} to match the naming in {@code IInventory}
	 */
	public static ItemStack getStackInSlotOnClosing(IInventory inventory, int slot) {
		return getAndRemove(inventory, slot);
	}

	/**
	 * generic implementation for {@link IInventory#getStackInSlotOnClosing}<br>
	 * gets the contents from the given slot and then empties it
	 *
	 * @param inventory the inventory
	 * @param slot      the slot to get the contents from
	 * @return the slots contents
	 */
	public static ItemStack getAndRemove(IInventory inventory, int slot) {
		ItemStack item = inventory.getStackInSlot(slot);
		inventory.setInventorySlotContents(slot, null);
		return item;
	}

	/**
	 * calls {@link #spill(net.minecraft.tileentity.TileEntity)} only if the given TileEntity implements {@link IInventory}
	 *
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
	 *
	 * @param te the TileEntity
	 */
	public static <T extends TileEntity & IInventory> void spill(T te) {
		spill(te.worldObj, te.xCoord, te.yCoord, te.zCoord, te);
	}

	/**
	 * spill the contents of the given Inventory at the given coordinates
	 *
	 * @param world     the world to spill in
	 * @param x         x coordinate
	 * @param y         y coordinate
	 * @param z         z coordinate
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
						itemEntity.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
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

	private static final String INV_KEY = "_sc$inventory";

	/**
	 * <p>Write an inventory to a (consistent) subkey of the NBTTagCompound.</p>
	 * <p>The inventory can be read back using {@link #readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound)}</p>
	 *
	 * @param stacks the ItemStacks to write
	 * @param nbt    the NBTTagCompound to write to
	 */
	public static void writeInventory(ItemStack[] stacks, NBTTagCompound nbt) {
		nbt.setTag(INV_KEY, writeInventory(stacks));
	}

	/**
	 * Serialize an {@link IInventory} to a {@link NBTTagList}<br>
	 * The contents may be read back with {@link #readInventory}
	 *
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
	 * Read an inventory from an NBTTagCompound. The inventory should be written with {@link #writeInventory(net.minecraft.item.ItemStack[])}
	 *
	 * @param stacks the array to read into
	 * @param nbt    the NBTTagCompound to read from
	 */
	public static void readInventory(ItemStack[] stacks, NBTTagCompound nbt) {
		readInventory0(stacks, nbt.getTagList(INV_KEY));
	}

	/**
	 * Deserialize an {@link IInventory} from a {@link NBTTagList}<br>
	 * The format of the NBT should match that produced by {@link #writeInventory}
	 *
	 * @param stacks  the array to be filled
	 * @param nbtList the NBTTagList containing the serialized contents
	 */
	public static void readInventory(ItemStack[] stacks, NBTTagList nbtList) {
		readInventory0(stacks, nbtList);
	}

	private static void readInventory0(ItemStack[] stacks, NBTTagList nbtList) {
		int len = stacks.length;
		for (NBTTagCompound nbt : NBT.<NBTTagCompound>asList(nbtList)) {
			ItemStack item = ItemStack.loadItemStackFromNBT(nbt);
			int idx = UnsignedShorts.toInt(nbt.getShort("slot"));
			if (idx < len) {
				stacks[idx] = item;
			} else {
				logger.severe(String.format("Inventory slot %d is out of bounds (length %d) while reading inventory %s",
						idx, len, SCReflection.getCallerClass(1).getName()));
			}
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
	 *
	 * @param inventory    the inventory to iterate
	 * @param includeNulls if empty ItemStacks should be included in the iterator
	 * @return an Iterator
	 */
	public static Iterator<ItemStack> iterator(final IInventory inventory, boolean includeNulls) {
		if (inventory instanceof Iterable) {
			@SuppressWarnings("unchecked")
			Iterator<ItemStack> it = ((Iterable<ItemStack>) inventory).iterator();
			return includeNulls ? it : Iterators.filter(it, Predicates.notNull());
		}
		return iterator0(inventory, includeNulls);
	}

	static Iterator<ItemStack> iterator0(final IInventory inventory, boolean includeNulls) {
		Iterator<ItemStack> it = new AbstractIterator<ItemStack>() {

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
	 * or the inventory itself, if it is already an Iterable (possibly filtered for nulls)
	 */
	public static Iterable<ItemStack> iterate(final IInventory inventory, final boolean includeNulls) {
		if (inventory instanceof Iterable) {
			@SuppressWarnings("unchecked")
			Iterable<ItemStack> it = (Iterable<ItemStack>) inventory;
			return includeNulls ? it : Iterables.filter(it, Predicates.notNull());
		}
		return new Iterable<ItemStack>() {

			@Override
			public Iterator<ItemStack> iterator() {
				return Inventories.iterator0(inventory, includeNulls);
			}
		};
	}

}
