package de.take_weiland.mods.commons.util;

import com.google.common.base.Objects;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.inv.Inventories;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.templates.TypedItemBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static de.take_weiland.mods.commons.util.Items.checkPhase;

@ParametersAreNonnullByDefault
public final class Blocks extends net.minecraft.init.Blocks {

    private Blocks() {
    }

    /**
     * <p>Equivalent to {@link #init(Block, String, Class)} using a default ItemBlock class.</p>
     *
     * @param block    the Block instance
     * @param baseName base name for this block
     */
    public static void init(Block block, String baseName) {
        init(block, baseName, getItemBlockClass(block));
    }

    /**
     * <p>Generic initialization for Blocks:</p>
     * <ul>
     * <li>Sets the Block's texture to <tt>modId:baseName</tt>, unless it is already set</li>
     * <li>Sets the Block's unlocalized name to <tt>modId.baseName</tt>, unless it is already set</li>
     * <li>Register the block with {@link GameRegistry#registerBlock(Block, Class, String)}</li>
     * <li>If the Block implements {@link HasSubtypes}, register ItemStacks for all subtypes using {@code GameRegistry.registerCustomItemStack}.</li>
     * </ul>
     * @param block     the Block instance
     * @param baseName  base name for this block
     * @param itemClass the ItemBlock class to use
     */
    public static void init(Block block, String baseName, Class<? extends ItemBlock> itemClass) {
        checkPhase("Block");
        String modId = Loader.instance().activeModContainer().getModId();

        if (SCReflector.instance.getRawIconName(block) == null) {
            block.setTextureName(modId + ":" + baseName);
        }
        if (SCReflector.instance.getRawUnlocalizedName(block) == null) {
            block.setUnlocalizedName(modId + "." + baseName);
        }

        GameRegistry.registerBlock(block, Objects.firstNonNull(itemClass, getItemBlockClass(block)), baseName);

        if (block instanceof HasSubtypes) {
            ItemStacks.registerSubstacks(baseName, getItem(block));
        }
    }

    public static Item getItem(Block block) {
        return Item.getItemFromBlock(block);
    }

    public static Block fromItem(Item item) {
        if (item instanceof ItemBlock) {
            return Block.getBlockFromItem(item);
        } else {
            throw new IllegalArgumentException("Not an ItemBlock");
        }
    }

    public static Block byID(int id) {
        return Block.getBlockById(id);
    }

    /**
     * <p>Generic implementation for {@link net.minecraft.block.Block#breakBlock}. This method drops the contents of any Inventory
     * associated with the block and should therefor be called before any TileEntity is removed.</p>
     *
     * @param block the Block instance
     * @param world the World
     * @param x     x position
     * @param y     y position
     * @param z     z position
     * @param meta  the metadata of the block being broken
     */
    public static void genericBreak(Block block, World world, int x, int y, int z, int meta) {
        if (block.hasTileEntity(meta)) {
            Inventories.spillIfInventory(world.getTileEntity(x, y, z));
        }
    }

    /**
     * <p>Generic implementation for {@link Block#getSubBlocks(Item, CreativeTabs, List)}.
     * This method takes the {@link de.take_weiland.mods.commons.meta.HasSubtypes} interface into account.</p>
     *
     * @param block the Block instance
     * @param list  the list to add ItemStacks to
     */
    public static void getSubBlocksImpl(Block block, List<ItemStack> list) {
        if (block instanceof HasSubtypes) {
            HasSubtypes.getSubBlocksImpl(block, list);
        } else {
            list.add(new ItemStack(block));
        }
    }

    private static <T extends Subtype> void getSubBlocksWithSubtypes(Block block, HasSubtypes<T> subtypes, List<ItemStack> list) {
        MetadataProperty<T> property = subtypes.subtypeProperty();
        for (T type : property.values()) {
            list.add(property.apply(type, new ItemStack(block)));
        }
    }

    private static Class<? extends ItemBlock> getItemBlockClass(Block block) {
        return block instanceof HasSubtypes ? TypedItemBlock.class : ItemBlock.class;
    }

}
