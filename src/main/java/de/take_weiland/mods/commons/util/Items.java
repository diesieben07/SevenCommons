package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static cpw.mods.fml.common.LoaderState.PREINITIALIZATION;

@ParametersAreNonnullByDefault
public final class Items extends net.minecraft.init.Items {

    /**
     * <p>Performs some generic initialization on the given Item:</p>
     * <ul>
     * <li>Sets the Item's texture to <tt>modId:baseName</tt>, unless it is already set</li>
     * <li>Sets the Item's unlocalized name to <tt>modId.baseName</tt>, unless it is already set</li>
     * <li>Register the Item with {@link GameRegistry#registerItem(Item, String)}</li>
     * <li>If the Item has subtypes (implementing {@link HasSubtypes}):
     * <ul>
     * <li>Call {@link Item#setHasSubtypes(boolean) setHasSubtypes(true)}</li>
     * <li>Register custom ItemStacks for the subtypes with {@link GameRegistry#registerCustomItemStack(String, ItemStack)}</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param item     the Item instance
     * @param baseName base name for this Item
     */
    public static void init(Item item, String baseName) {
        checkPhase("Item");

        String modId = Loader.instance().activeModContainer().getModId();

        if (SCReflector.instance.getRawIconName(item) == null) {
            item.setTextureName(modId + ":" + baseName);
        }

        if (SCReflector.instance.getRawUnlocalizedName(item) == null) {
            item.setUnlocalizedName(modId + "." + baseName);
        }

        if (item instanceof HasSubtypes) {
            SCReflector.instance.setHasSubtypes(item, true);

            ItemStacks.registerSubstacks(baseName, item);
        }

        GameRegistry.registerItem(item, baseName);
    }

    /**
     * <p>Utility function to initialize a lot of Items at the same time.</p>
     *
     * @param baseNameFunction a function that provides the base name for each Item
     * @param items            the list of items
     */
    @SafeVarargs
    public static <T extends Item> void initAll(Function<? super T, ? extends String> baseNameFunction, T... items) {
        initAll(baseNameFunction, null, items);
    }

    /**
     * <p>Utility function to initialize a lot of Items at the same time and set their creative tab.</p>
     *
     * @param baseNameFunction a function that provides the base name for each Item
     * @param tab              the creative tab
     * @param items            the list of items
     */
    @SafeVarargs
    public static <T extends Item> void initAll(Function<? super T, ? extends String> baseNameFunction, @Nullable CreativeTabs tab, T... items) {
        for (T item : items) {
            init(item, baseNameFunction.apply(item));
            if (tab != null) {
                item.setCreativeTab(tab);
            }
        }
    }

    public static Block getBlock(Item item) {
        return Blocks.fromItem(item);
    }

    public static Item forBlock(Block block) {
        return Blocks.getItem(block);
    }

    public static Item byID(int id) {
        return Item.getItemById(id);
    }

    private Items() {
    }

    static void checkPhase(String type) {
        checkState(Loader.instance().isInState(PREINITIALIZATION), "Mod %s tried to register a %s outside of the preInit phase", Loader.instance().activeModContainer().getModId(), type);
    }
}
