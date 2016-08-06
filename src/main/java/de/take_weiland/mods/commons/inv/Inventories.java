package de.take_weiland.mods.commons.inv;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * <p>A collection of static utility methods regarding implementors of {@link IInventory}.</p>
 *
 * @author diesieben07
 */
public final class Inventories {

    private static final EnumSet<EnumFacing> ALL_SIDES = EnumSet.allOf(EnumFacing.class);

    /**
     * <p>Spill any inventory of a {@link TileEntity} into the world. Usually used in {@link Block#breakBlock}.</p>
     *
     * @param te the TileEntity
     */
    public static void spill(TileEntity te) {
        BlockPos pos = te.getPos();
        spill(te.getWorld(), pos.getX(), pos.getY(), pos.getZ(), te);
    }

    /**
     * <p>Spill the contents of any inventory provided by the given capability provider at the given coordinates.</p>
     *
     * @param world    the world
     * @param x        x coordinate
     * @param y        y coordinate
     * @param z        z coordinate
     * @param provider the capability provider
     */
    public static void spill(World world, double x, double y, double z, ICapabilityProvider provider) {
        if (sideOf(world).isServer()) {
            IItemHandler inventory = getInventory(provider, null);
            if (inventory != null) {
                Random rand = world.rand;

                for (int slot = 0, n = inventory.getSlots(); slot < n; slot++) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if (stack == null) continue;

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
    }

    private static IItemHandler getInventory(ICapabilityProvider provider, @Nullable EnumFacing side) {
        IItemHandler handler = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        if (handler != null) {
            return handler;
        } else if (provider instanceof ISidedInventory) {
            return new SidedInvWrapper(((ISidedInventory) provider), side);
        } else if (provider instanceof IInventory) {
            return new InvWrapper((IInventory) provider);
        } else {
            return null;
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
    public static ItemStack tryStore(ItemStack stack, World world, BlockPos pos) {
        return tryStore(stack, world, pos, ALL_SIDES, null);
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
    public static ItemStack tryStore(ItemStack stack, World world, BlockPos pos, @Nullable EnumFacing dispenseSide) {
        return tryStore(stack, world, pos, ALL_SIDES, dispenseSide);
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
    public static ItemStack tryStore(ItemStack stack, World world, BlockPos pos, EnumSet<EnumFacing> sides, @Nullable EnumFacing dispenseSide) {
        @Nullable
        ItemStack result = stack;
        for (EnumFacing side : sides) {
            result = tryStoreInInv(result, world, pos.offset(side), side.getOpposite());
            if (result == null) {
                return null;
            }
        }
        if (dispenseSide != null) {
            EntityItem item = new EntityItem(
                    world,
                    itemEntityPos(pos.getX(), dispenseSide.getFrontOffsetX()),
                    itemEntityPos(pos.getY(), dispenseSide.getFrontOffsetY()),
                    itemEntityPos(pos.getZ(), dispenseSide.getFrontOffsetZ()),
                    stack.copy()
            );
            item.motionX = dispenseSide.getFrontOffsetX() / 8d;
            item.motionY = dispenseSide.getFrontOffsetY() / 8d;
            item.motionZ = dispenseSide.getFrontOffsetZ() / 8d;
            world.spawnEntityInWorld(item);
            return null;
        }
        return result;
    }

    private static double itemEntityPos(int base, int offset) {
        return base + 0.5 + (offset * 0.5);
    }

    private static ItemStack tryStoreInInv(ItemStack stack, World world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            return stack;
        } else {
            return ItemHandlerHelper.insertItem(getInventory(te, side), stack, false);
        }
    }

    private Inventories() {
    }
}
