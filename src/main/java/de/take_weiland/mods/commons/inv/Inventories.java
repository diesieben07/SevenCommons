package de.take_weiland.mods.commons.inv;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.client.I18n;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * A collection of static utility methods regarding implementors of {@link IInventory}
 *
 * @author diesieben07
 */
public final class Inventories {

	/**
	 * <p>Generic implementation for {@link net.minecraft.inventory.IInventory#decrStackSize}.</p>
	 *
	 * @param inventory the inventory
	 * @param slot      the slot to be decreased in size
	 * @param count     the number of items to be depleted
	 * @return the stack being depleted from the inventory
	 */
	public static ItemStack decreaseStackSize(IInventory inventory, @Nonnegative int slot, @Nonnegative int count) {
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
					inventory.markDirty();
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
		return inv.hasCustomInventoryName() ? inv.getInventoryName() : I18n.translate(inv.getInventoryName());
	}

	/**
	 * <p>Generic implementation for {@link net.minecraft.inventory.IInventory#getStackInSlotOnClosing}.
	 * Gets the contents from the given slot and then empties the slot.</p>
	 *
	 * @param inventory the inventory
	 * @param slot      the slot to get the contents from
	 * @return the slots contents
	 */
	public static ItemStack getAndRemove(IInventory inventory, @Nonnegative int slot) {
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
			spill(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, (IInventory) te);
		}
	}

	/**
	 * <p>Spill the contents of a {@link TileEntity} that also implements {@link IInventory} into the world. Usually used in {@link Block#breakBlock}.</p>
	 *
	 * @param te the TileEntity
	 */
	public static <T extends TileEntity & IInventory> void spill(T te) {
		spill(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, te);
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
				float xRand = rand.nextFloat() * 0.8F + 0.1F;
				float yRand = rand.nextFloat() * 0.8F + 0.1F;
				float zRand = rand.nextFloat() * 0.8F + 0.1F;

				while (stack.stackSize > 0) {
					int partialStackSize = Math.min(stack.stackSize, rand.nextInt(21) + 10);

					ItemStack stackToSpawn = stack.splitStack(partialStackSize);
					EntityItem itemEntity = new EntityItem(world, x + xRand, y + yRand, z + zRand, stackToSpawn);

					itemEntity.motionX = rand.nextGaussian() * 0.05F;
					itemEntity.motionY = rand.nextGaussian() * 0.05F + 0.2F;
					itemEntity.motionZ = rand.nextGaussian() * 0.05F;
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
	 * @param key the key to writeTo to
	 */
	public static void writeInventory(ItemStack[] stacks, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeInventory(stacks));
	}

	/**
	 * <p>Write the given inventory to an NBTTagList. The contents can be read with either
	 * {@link #readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound, String)} or
	 * {@link #readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagList)}.</p>
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
				itemCompound.setInteger("slot", i);
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
		readInventory(stacks, nbt.getTagList(key, Constants.NBT.TAG_COMPOUND));
	}

	/**
	 * <p>Read the given inventory from the given NBTTagList. The contents must be in the format produced by {@link #writeInventory(net.minecraft.item.ItemStack[])}
	 * or {@link #writeInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound, String)}.</p>
	 *
	 * @param stacks the inventory
	 * @param nbtList the NBTTagList
	 */
	public static void readInventory(ItemStack[] stacks, NBTTagList nbtList) {
		int invSize = stacks.length;
		int listLen = nbtList.tagCount();
		for (int i = 0; i < listLen; i++) {
			NBTTagCompound itemCompound = nbtList.getCompoundTagAt(i);

			ItemStack item = ItemStack.loadItemStackFromNBT(itemCompound);
			int idx = itemCompound.getInteger("slot");
			if (idx < invSize && idx > 0) {
				stacks[idx] = item;
			}
		}
	}

	/**
	 * <p>Create an {@code Iterator} that iterates all slots in the given inventory, in order.</p>
	 * @param inventory the inventory
	 * @return an Iterator
	 */
	public static UnmodifiableIterator<ItemStack> iterator(final IInventory inventory) {
		return iterator(inventory, true);
	}

	/**
	 * <p>Create an {@code Iterator} that iterates over the given inventory, in order.</p>
	 *
	 * @param inventory the inventory
	 * @param includeNulls whether null (empty slots) should be included in the iterator
	 * @return an Iterator
	 */
	@SuppressWarnings("unchecked")
	public static UnmodifiableIterator<ItemStack> iterator(IInventory inventory, boolean includeNulls) {
		UnmodifiableIterator<ItemStack> it = new InventoryIterator(inventory);
		return includeNulls ? it : Iterators.filter(it, Predicates.notNull());
	}

	/**
	 * <p>Create an {@code Iterable} that represents the given inventory. The created Iterable uses {@link #iterator(net.minecraft.inventory.IInventory)}
	 * to create new Iterators.</p>
	 * @param inventory the inventory
	 * @return an Iterable
	 */
	public static FluentIterable<ItemStack> iterate(IInventory inventory) {
		return iterate(inventory, true);
	}

	/**
	 * <p>Create an {@code Iterable} that represents the given inventory. The created Iterable uses {@link #iterator(net.minecraft.inventory.IInventory, boolean)}
	 * to create new Iterators.</p>
	 * @param inventory the inventory
	 * @param includeNulls whether null (empty slots) should be included in the iterator
	 * @return an Iterable
	 */
	public static FluentIterable<ItemStack> iterate(final IInventory inventory, final boolean includeNulls) {
		return new FluentIterable<ItemStack>() {
			@Nonnull
			@Override
			public Iterator<ItemStack> iterator() {
				return Inventories.iterator(inventory, includeNulls);
			}
		};
	}

	private static final class InventoryIterator extends UnmodifiableIterator<ItemStack> {

		private final IInventory inv;
		private int next;

		InventoryIterator(IInventory inv) {
			this.inv = inv;
		}

		@Override
		public boolean hasNext() {
			return next < inv.getSizeInventory();
		}

		@Override
		public ItemStack next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return inv.getStackInSlot(next++);
		}
	}

	private Inventories() { }

}
