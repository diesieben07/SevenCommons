package de.take_weiland.mods.commons.inv;

import com.google.common.base.Predicates;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.client.I18n;
import de.take_weiland.mods.commons.internal.InvIteratorMarker;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.util.MiscUtil;
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

	private static final Logger logger = MiscUtil.getLogger("SC|Inventories");

	/**
	 * <p>Generic implementation for {@link IInventory#decrStackSize}.</p>
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

	/**
	 * <p>Get the display name for the given Inventory. This method may only be called from client code.</p>
	 * @param inv the inventory
	 * @return the display name
	 */
	@SideOnly(Side.CLIENT)
	public static String getDisplayName(IInventory inv) {
		return inv.isInvNameLocalized() ? inv.getInvName() : I18n.translate(inv.getInvName());
	}

	/**
	 * <p>Generic implementation for {@link IInventory#getStackInSlotOnClosing}.
	 * Gets the contents from the given slot and then empties the slot.</p>
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
	 * <p>Calls {@link #spill(net.minecraft.tileentity.TileEntity)} only if the given TileEntity implements {@link IInventory}.</p>
	 *
	 * @param te the TileEntity
	 */
	public static void spillIfInventory(TileEntity te) {
		if (te instanceof IInventory) {
			spill(te.worldObj, te.xCoord, te.yCoord, te.zCoord, (IInventory) te);
		}
	}

	/**
	 * <p>Spill the contents of a {@link TileEntity} that also implements {@link IInventory} into the world. Usually used in {@link Block#breakBlock}.</p>
	 *
	 * @param te the TileEntity
	 */
	public static <T extends TileEntity & IInventory> void spill(T te) {
		spill(te.worldObj, te.xCoord, te.yCoord, te.zCoord, te);
	}

	/**
	 * <p>Spill the contents of the given Inventory at the given coordinates.</p>
	 *
	 * @param world     the world
	 * @param x         x coordinate
	 * @param y         y coordinate
	 * @param z         z coordinate
	 * @param inventory the inventory
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

	/**
	 * <p>Write the given inventory to the given key in the NBTTagCompound. The contents can be read with either
	 * {@link #readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound, String)} or
	 * {@link #readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagList)}.</p>
	 * @param stacks the inventory
	 * @param nbt the NBTTagCompound
	 * @param key the key to write to
	 */
	public static void writeInventory(ItemStack[] stacks, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeInventory(stacks));
	}

	/**
	 * <p>Write the given inventory to an NBTTagList.</p>
	 *
	 * @param stacks the inventory
	 * @return an NBTTagList
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
	 * <p>Read the given inventory from the given key in the NBTTagCompound. The contents must be in the format produced by
	 * {@link #writeInventory(net.minecraft.item.ItemStack[])} or {@link #writeInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound, String)}.</p>
	 *
	 * @param stacks the inventory
	 * @param nbt the NBTTagCompound
	 * @param key the key to read from
	 */
	public static void readInventory(ItemStack[] stacks, NBTTagCompound nbt, String key) {
		readInventory(stacks, nbt.getTagList(key));
	}

	/**
	 * <p>Read the given inventory from the given NBTTagList. The contents must be in the format produced by {@link #writeInventory(net.minecraft.item.ItemStack[])}
	 * or {@link #writeInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound, String)}.</p>
	 *
	 * @param stacks the inventory
	 * @param nbtList the NBTTagList
	 */
	public static void readInventory(ItemStack[] stacks, NBTTagList nbtList) {
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
	 * <p>Create an {@code Iterator} that iterates all non-null slots in the given inventory, in order.</p>
	 * @param inventory the inventory
	 * @return an Iterator
	 */
	public static Iterator<ItemStack> iterator(final IInventory inventory) {
		return iterator(inventory, true);
	}

	/**
	 * <p>Create an {@code Iterator} that iterates over the given inventory, in order.</p>
	 *
	 * @param inventory the inventory
	 * @param includeNulls whether null (empty slots) should be included in the iterator
	 * @return an Iterator
	 */
	public static Iterator<ItemStack> iterator(final IInventory inventory, boolean includeNulls) {
		if (inventory instanceof InvIteratorMarker) {
			@SuppressWarnings("unchecked")
			Iterator<ItemStack> it = ((Iterable<ItemStack>) inventory).iterator();
			return includeNulls ? it : Iterators.filter(it, Predicates.notNull());
		} else {
			return iterator0(inventory, includeNulls);
		}
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
	 * <p>Create an {@code Iterable} that represents the given inventory. The created Iterable uses {@link #iterator(net.minecraft.inventory.IInventory)}
	 * to create new Iterators.</p>
	 * @param inventory the inventory
	 * @return an Iterable
	 */
	public static Iterable<ItemStack> iterate(IInventory inventory) {
		return iterate(inventory, true);
	}

	/**
	 * <p>Create an {@code Iterable} that represents the given inventory. The created Iterable uses {@link #iterator(net.minecraft.inventory.IInventory, boolean)}
	 * to create new Iterators.</p>
	 * @param inventory the inventory
	 * @param includeNulls whether null (empty slots) should be included in the iterator
	 */
	public static Iterable<ItemStack> iterate(final IInventory inventory, final boolean includeNulls) {
		if (inventory instanceof InvIteratorMarker) {
			@SuppressWarnings("unchecked")
			Iterable<ItemStack> it = (Iterable<ItemStack>) inventory;
			return includeNulls ? it : Iterables.filter(it, Predicates.notNull());
		} else {
			return new FluentIterable<ItemStack>() {

				@Override
				public Iterator<ItemStack> iterator() {
					return Inventories.iterator0(inventory, includeNulls);
				}
			};
		}
	}

	private Inventories() { }

}
