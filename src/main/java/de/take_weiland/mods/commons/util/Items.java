package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.InstanceCacheHolder;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import net.minecraft.item.Item;

import static de.take_weiland.mods.commons.util.RegistrationUtil.checkPhase;

public final class Items {

	private Items() {
	}

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

			ItemStacks.registerSubstacks(baseName, item, InstanceCacheHolder.ITEM_STACK_FUNCTION);
		}

		GameRegistry.registerItem(item, baseName);
	}

	public static Item byId(int id) {
		if (id >= 0 && id <= 32000) {
			Item item = Item.itemsList[id];
			if (item == null) {
				throw new IllegalArgumentException("Unknown ItemID " + id);
			}
			return item;
		}
		throw new IllegalArgumentException("Invalid ItemID " + id);
	}

}
