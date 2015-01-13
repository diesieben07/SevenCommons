package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagShort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static de.take_weiland.mods.commons.util.RegistrationUtil.checkPhase;

@ParametersAreNonnullByDefault
public final class Items {

	private Items() { }

	/**
	 * <p>Equivalent to {@link #init(net.minecraft.item.Item, String, String)} with the currently active ModId.</p>
	 *
	 * @param item     the Item instance
	 * @param baseName base name for this Item
	 */
	public static void init(Item item, String baseName) {
		// check phase here already so that we don't fail with a NPE on activeModContainer instead
		checkPhase("Item");
		init(item, baseName, Loader.instance().activeModContainer().getModId());
	}

	/**
	 * <p>Performs some generic initialization on the given Item:</p>
	 * <ul>
	 * <li>Sets the Item's texture to <tt>modId:baseName</tt>, unless it is already set</li>
	 * <li>Sets the Item's unlocalized name to <tt>modId.baseName</tt>, unless it is already set</li>
	 * <li>Register the Item with {@link cpw.mods.fml.common.registry.GameRegistry#registerItem(net.minecraft.item.Item, String, String)}</li>
	 * <li>If the Item has subtypes (implementing {@link de.take_weiland.mods.commons.meta.HasSubtypes}):
	 * <ul>
	 * <li>Call {@link Item#setHasSubtypes(boolean) setHasSubtypes(true)}</li>
	 * <li>Register custom ItemStacks for the subtypes with {@link cpw.mods.fml.common.registry.GameRegistry#registerCustomItemStack(String, net.minecraft.item.ItemStack)}</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param item     the Item instance
	 * @param baseName base name for this Item
	 * @param modId    your ModId
	 */
	public static void init(Item item, String baseName, String modId) {
		checkPhase("Item");

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

	public static Block getBlock(Item item) {
		return Blocks.fromItem(item);
	}

	public static Item forBlock(Block block) {
		return Blocks.getItem(block);
	}

	public static Item byID(int id) {
		return Item.itemsList[id];
	}

	private static final short ITEM_NULL_ID = -1;

	private static final ByteStreamSerializer<Item> streamSerializer = new ByteStreamSerializer<Item>() {
		@Override
		public void write(@Nullable Item instance, @Nonnull MCDataOutputStream out) {
			out.writeShort(instance == null ? ITEM_NULL_ID : instance.itemID);
		}

		@Override
		public Item read(MCDataInputStream in) {
			int id = in.readShort();
			return id == ITEM_NULL_ID ? null : Item.itemsList[id];
		}
	};

	private static final NBTSerializer<Item> nbtSerializer = new NBTSerializer<Item>() {
		@Override
		public NBTBase serialize(@Nullable Item instance) {
			return instance == null ? NBTData.serializedNull() : new NBTTagShort("", (short) instance.itemID);
		}

		@Override
		public Item deserialize(NBTBase nbt) {
			return NBTData.isSerializedNull(nbt) ? null : Item.itemsList[((NBTTagShort) nbt).data];
		}
	};
}
