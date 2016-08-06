package de.take_weiland.mods.commons.util;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public final class Registration {

    public static <T extends IForgeRegistryEntry<T>> void init(T entry, String name) {
        init(entry, name, null);
    }

    public static <T extends IForgeRegistryEntry<T>> void init(T entry, String name, @Nullable CreativeTabs creativeTab) {
        if (name.indexOf(':') >= 0) {
            throw new IllegalArgumentException("Name cannot contain ':'");
        }
        ModContainer mod = Loader.instance().activeModContainer();
        entry.setRegistryName(new ResourceLocation(mod.getModId(), name));

        EntryAccess<T> entryAccess = getEntryAccess(entry);
        if (entryAccess != null && entryAccess.getRawUnlocalizedName(entry) == null) {
            entryAccess.setUnlocalizedName(entry, mod.getModId() + '.' + name);
            if (creativeTab != null) {
                entryAccess.setCreativeTab(entry, creativeTab);
            }
        }

        GameRegistry.register(entry);
        if (entry instanceof ItemBlockProvider) {
            init(((ItemBlockProvider) entry).createItemBlock(), name, creativeTab);
        }
    }

    public static <T extends IForgeRegistryEntry<T>> void initAll(Function<? super T, String> nameProvider, @Nullable CreativeTabs creativeTab, Iterable<? extends T> entries) {
        for (T entry : entries) {
            init(entry, nameProvider.apply(entry), creativeTab);
        }
    }

    private static final Map<Class<?>, EntryAccess<?>> entryAccesses = ImmutableMap.of(
            Block.class, new BlockEntryAccess(),
            Item.class, new ItemEntryAccess()
    );

    public interface ItemBlockProvider {

        @Nullable
        ItemBlock createItemBlock();

    }

    private static <T extends IForgeRegistryEntry<T>> EntryAccess<T> getEntryAccess(T entry) {
        //noinspection unchecked
        return (EntryAccess<T>) entryAccesses.get(entry.getRegistryType());
    }

    private static final Field blockUnlocalizedName, itemUnlocalizedName;

    static {
        try {
            blockUnlocalizedName = Block.class.getDeclaredField(MCPNames.field(SRGConstants.F_UNLOCALIZED_NAME_BLOCK));
            itemUnlocalizedName = Item.class.getDeclaredField(MCPNames.field(SRGConstants.F_UNLOCALIZED_NAME_ITEM));
            blockUnlocalizedName.setAccessible(true);
            itemUnlocalizedName.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw Throwables.propagate(e);
        }
    }

    private interface EntryAccess<T extends IForgeRegistryEntry<T>> {

        default String getRawUnlocalizedName(T entry) {
            try {
                return getRawUnlocalizedName0(entry);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        String getRawUnlocalizedName0(T entry) throws Exception;

        void setUnlocalizedName(T entry, String name);

        void setCreativeTab(T entry, CreativeTabs tab);

    }

    private static final class BlockEntryAccess implements EntryAccess<Block> {

        BlockEntryAccess() {}

        @Override
        public String getRawUnlocalizedName0(Block entry) throws IllegalAccessException {
            return (String) blockUnlocalizedName.get(entry);
        }

        @Override
        public void setUnlocalizedName(Block entry, String name) {
            entry.setUnlocalizedName(name);
        }

        @Override
        public void setCreativeTab(Block entry, CreativeTabs tab) {
            entry.setCreativeTab(tab);
        }
    }

    private static final class ItemEntryAccess implements EntryAccess<Item> {

        ItemEntryAccess() {}

        @Override
        public String getRawUnlocalizedName0(Item entry) throws IllegalAccessException {
            return (String) itemUnlocalizedName.get(entry);
        }

        @Override
        public void setUnlocalizedName(Item entry, String name) {
            entry.setUnlocalizedName(name);
        }

        @Override
        public void setCreativeTab(Item entry, CreativeTabs tab) {
            entry.setCreativeTab(tab);
        }
    }
}
