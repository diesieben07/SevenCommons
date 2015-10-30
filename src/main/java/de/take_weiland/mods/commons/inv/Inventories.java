package de.take_weiland.mods.commons.inv;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.client.I18n;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * <p>A collection of static utility methods regarding implementors of {@link IInventory}.</p>
 *
 * @author diesieben07
 */
public final class Inventories {

    private static final EnumSet<ForgeDirection> ALL_SIDES = EnumSet.copyOf(Arrays.asList(ForgeDirection.VALID_DIRECTIONS));

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
     *
     * @param inv the inventory
     * @return the display name
     */
    @SideOnly(Side.CLIENT)
    public static String getDisplayName(IInventory inv) {
        return inv.isCustomInventoryName() ? inv.getInventoryName() : I18n.translate(inv.getInventoryName());
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
            spill(te.getWorld(), te.xCoord, te.yCoord, te.zCoord, (IInventory) te);
        }
    }

    /**
     * <p>Spill the contents of a {@link TileEntity} that also implements {@link IInventory} into the world. Usually used in {@link Block#breakBlock}.</p>
     *
     * @param te the TileEntity
     */
    public static <T extends TileEntity & IInventory> void spill(T te) {
        spill(te.getWorld(), te.xCoord, te.yCoord, te.zCoord, te);
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
        if (sideOf(world).isServer()) {
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

    static final String NBT_KEY = "_sc$inventory";
    static final String CUSTOM_NAME_KEY = "_sc$customName";

    /**
     * <p>Write the given inventory to the given key in the NBTTagCompound. The contents can be read with either
     * {@link #readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound, String)} or
     * {@link #readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagList)}.</p>
     *
     * @param stacks the inventory
     * @param nbt    the NBTTagCompound
     * @param key    the key to writeTo to
     */
    public static void writeInventory(ItemStack[] stacks, NBTTagCompound nbt, String key) {
        nbt.setTag(key, writeInventory(stacks));
    }

    /**
     * <p>Write the given inventory to an NBTTagList. The contents can be read with either
     * {@link #readInventory(ItemStack[], NBTTagCompound, String)} or
     * {@link #readInventory(ItemStack[], NBTTagList)}.</p>
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
     * {@link #writeInventory(ItemStack[])} or {@link #writeInventory(ItemStack[], NBTTagCompound, String)}.</p>
     *
     * @param stacks the inventory
     * @param nbt    the NBTTagCompound
     * @param key    the key to read from
     */
    public static void readInventory(ItemStack[] stacks, NBTTagCompound nbt, String key) {
        readInventory(stacks, nbt.getTagList(key, Constants.NBT.TAG_COMPOUND));
    }

    /**
     * <p>Read the given inventory from the given NBTTagList. The contents must be in the format produced by {@link #writeInventory(net.minecraft.item.ItemStack[])}
     * or {@link #writeInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound, String)}.</p>
     *
     * @param stacks  the inventory
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
     *
     * @param inventory the inventory
     * @return an Iterator
     */
    public static UnmodifiableIterator<ItemStack> iterator(final IInventory inventory) {
        return iterator(inventory, true);
    }

    /**
     * <p>Create an {@code Iterator} that iterates over the given inventory, in order.</p>
     *
     * @param inventory    the inventory
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
     *
     * @param inventory the inventory
     * @return an Iterable
     */
    public static Iterable<ItemStack> iterate(IInventory inventory) {
        return iterate(inventory, true);
    }

    /**
     * <p>Create an {@code Iterable} that represents the given inventory. The created Iterable uses {@link #iterator(net.minecraft.inventory.IInventory, boolean)}
     * to create new Iterators.</p>
     *
     * @param inventory    the inventory
     * @param includeNulls whether null (empty slots) should be included in the iterator
     * @return an Iterable
     */
    public static Iterable<ItemStack> iterate(final IInventory inventory, final boolean includeNulls) {
        Iterable<ItemStack> iterable = inventory instanceof SimpleInventory
                ? ((SimpleInventory) inventory)
                : new InventoryAsIterable(inventory);

        return includeNulls ? iterable : Iterables.filter(iterable, Predicates.notNull());
    }

    static void doForEach(IInventory self, Consumer<? super ItemStack> action) {
        for (int i = 0, size = self.getSizeInventory(); i < size; i++) {
            action.accept(self.getStackInSlot(i));
        }
    }

    /**
     * <p>Try to store the given ItemStack in any inventory adjacent to the given block position.</p>
     *
     * @param stack the ItemStack to store
     * @param world the world
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     * @return any leftover items that could not be stored or null if the whole stack could be stored
     */
    @Nullable
    public static ItemStack tryStore(ItemStack stack, World world, int x, int y, int z) {
        return tryStore(stack, world, x, y, z, ALL_SIDES, null);
    }

    /**
     * <p>Try to store the given ItemStack in any inventory adjacent to the given block position. If {@code dispenseSide}
     * is not null and not enough applicable inventory space is available, dispense it as an item entity on the specified side of the block.</p>
     *
     * @param stack        the ItemStack to store
     * @param world        the world
     * @param x            x coordinate
     * @param y            y coordinate
     * @param z            z coordinate
     * @param dispenseSide the side to dispense any leftover items
     * @return any leftover items that could not be stored or dispensed or null if the whole stack could be stored or dispensed
     */
    public static ItemStack tryStore(ItemStack stack, World world, int x, int y, int z, @Nullable ForgeDirection dispenseSide) {
        return tryStore(stack, world, x, y, z, ALL_SIDES, dispenseSide);
    }

    /**
     * <p>Try to store the given ItemStack in the inventories adjacent to the given block position on the specified sides.
     * If {@code dispenseSide} is not null and not enough applicable inventory space is available, dispense it as an item
     * entity on the specified side of the block.</p>
     *
     * @param stack        the ItemStack to store
     * @param world        the world
     * @param x            x coordinate
     * @param y            y coordinate
     * @param z            z coordinate
     * @param sides        the sides of the block to check for inventories
     * @param dispenseSide the side to dispense any leftover items
     * @return any leftover items that could not be stored or dispensed or null if the whole stack could be stored or dispensed
     */
    @Nullable
    public static ItemStack tryStore(ItemStack stack, World world, int x, int y, int z, EnumSet<ForgeDirection> sides, @Nullable ForgeDirection dispenseSide) {
        @Nullable
        ItemStack result = stack;
        for (ForgeDirection side : sides) {
            result = tryStoreInInv(result, world, x + side.offsetX, y + side.offsetY, z + side.offsetZ, side.getOpposite());
            if (result == null) {
                return null;
            }
        }
        if (dispenseSide != null) {
            EntityItem item = new EntityItem(world, itemEntityPos(x, dispenseSide.offsetX), itemEntityPos(y, dispenseSide.offsetY), itemEntityPos(z, dispenseSide.offsetZ), stack.copy());
            item.motionX = dispenseSide.offsetX / 8d;
            item.motionY = dispenseSide.offsetY / 8d;
            item.motionZ = dispenseSide.offsetZ / 8d;
            world.spawnEntityInWorld(item);
            return null;
        }
        return result;
    }

    private static double itemEntityPos(int base, int offset) {
        if (offset == 1) {
            return base + 1;
        } else if (offset == -1) {
            return base;
        } else {
            return base + 0.5;
        }
    }

    private static ItemStack tryStoreInInv(ItemStack stack, World world, int x, int y, int z, ForgeDirection side) {
        Block block = world.getBlock(x, y, z);
        IInventory inv;

        TileEntity te;
        if (block instanceof BlockChest) {
            inv = ((BlockChest) block).getInventory(world, x, y, z);
        } else if (!((te = world.getTileEntity(x, y, z)) instanceof IInventory)) {
            return stack;
        } else {
            inv = (IInventory) te;
        }

        boolean isSided = inv instanceof ISidedInventory;
        int sideInt = side.ordinal();

        InvIterator it = isSided ? sidedInvIterator((ISidedInventory) inv, sideInt) : invSlotsIterator(inv);

        while (true) {
            int slot = it.next();
            if (slot == -1) break;

            ItemStack invStack = inv.getStackInSlot(slot);
            if (invStack == null || !ItemStacks.equal(invStack, stack)) {
                continue;
            }
            if (!inv.isItemValidForSlot(slot, stack) || (isSided && !((ISidedInventory) inv).canInsertItem(slot, stack, sideInt))) {
                continue;
            }

            ItemStack newStack = ItemStacks.merge(stack, invStack, true);
            inv.setInventorySlotContents(slot, newStack);

            if (stack.stackSize == 0) {
                return null;
            }
        }

        it.reset();
        while (true) {
            int slot = it.next();
            if (slot == -1) break;

            ItemStack invStack = inv.getStackInSlot(slot);
            if (invStack != null) {
                continue;
            }
            if (!inv.isItemValidForSlot(slot, stack) || (isSided && !((ISidedInventory) inv).canInsertItem(slot, stack, sideInt))) {
                continue;
            }

            inv.setInventorySlotContents(slot, stack);
            return null;
        }

        return stack;
    }

    private static InvIterator sidedInvIterator(ISidedInventory inv, int side) {
        int[] slots = inv.getSlotsForFace(side);

        return new InvIterator() {

            private int index;

            @Override
            public int next() {
                if (index == slots.length) {
                    return -1;
                } else {
                    return slots[index++];
                }
            }

            @Override
            public void reset() {
                index = 0;
            }
        };
    }

    private static InvIterator invSlotsIterator(IInventory inv) {
        int max = inv.getSizeInventory();
        return new InvIterator() {

            private int curr;

            @Override
            public int next() {
                return curr == max ? -1 : curr++;
            }

            @Override
            public void reset() {
                curr = 0;
            }
        };
    }

    private interface InvIterator {

        int next();

        void reset();

    }

    private static class InventoryAsIterable implements Iterable<ItemStack> {

        private final IInventory inventory;

        InventoryAsIterable(IInventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Iterator<ItemStack> iterator() {
            return new InventoryIterator(inventory);
        }

        @Override
        public void forEach(Consumer<? super ItemStack> action) {
            doForEach(inventory, action);
        }

        @Override
        public Spliterator<ItemStack> spliterator() {
            return new InventorySpliterator(inventory, 0, inventory.getSizeInventory());
        }
    }

    private Inventories() {
    }
}
