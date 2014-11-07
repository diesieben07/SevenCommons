package de.take_weiland.mods.commons.inv;

import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;


/**
 * <p>An inventory that stores its contents to an {@link net.minecraft.item.ItemStack}.</p>
 */
public class ItemInventory extends AbstractInventory {

	static final String NBT_UUID_KEY = "_sc$iteminv$uuid";

	final UUID uuid = UUID.randomUUID();

	/**
	 * the ItemStack this inventory stores it's data to
	 */
	protected final ItemStack stack;
	/**
	 * the NBT key to store data in
	 */
	protected final String nbtKey;

	/**
	 * <p>This constructor uses the given NBT key to store the data.</p>
	 * <p>This constructor calls {@link #getSizeInventory()} to determine the size of the inventory. It needs to be overridden and work properly when called from this constructor.</p>
	 *
	 * @param stack  the ItemStack to save to
	 * @param nbtKey the NBT key to use
	 */
	protected ItemInventory(ItemStack stack, String nbtKey) {
		super();
		this.stack = stack;
		this.nbtKey = nbtKey;
		writeUUID();
		readFromNbt(getNbt());
	}

	/**
	 * <p>This constructor uses the given NBT key to store the data.</p>
	 *
	 * @param size   the size of this inventory
	 * @param stack  the ItemStack to save to
	 * @param nbtKey the NBT key to use
	 */
	protected ItemInventory(int size, ItemStack stack, String nbtKey) {
		super(size);
		this.stack = stack;
		this.nbtKey = nbtKey;
		writeUUID();
		readFromNbt(getNbt());
	}

	/**
	 * <p>This constructor uses the NBT key <tt>&lt;ModID&gt;:&lt;ItemName&gt;.inv</tt> to store the data.</p>
	 * <p>This constructor calls {@link #getSizeInventory()} to determine the size of the inventory. It needs to be
	 * overridden and work properly when called from this constructor.</p>
	 *
	 * @param stack the ItemStack to save to
	 */
	protected ItemInventory(ItemStack stack) {
		this(stack, defaultNBTKey(stack));
	}

	/**
	 * <p>This constructor uses the NBT key <tt>&lt;ModID&gt;:&lt;ItemName&gt;.inv</tt> to store the data.</p>
	 *
	 * @param size the size of this inventory
	 * @param stack the ItemStack to save to
	 */
	protected ItemInventory(int size, ItemStack stack) {
		this(size, stack, defaultNBTKey(stack));
	}

	private void writeUUID() {
		NBT.writeUUID(uuid, ItemStacks.getNbt(stack), nbtKey);
	}

	private static String defaultNBTKey(ItemStack stack) {
		GameRegistry.UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor(stack.getItem());
		return ui.modId + ":" + ui.name + ".inv";
	}

	@Override
	public void onInventoryChanged() {
		super.onInventoryChanged();
		saveData();
	}

	@Override
	public void closeChest() {
		super.closeChest();
		if (stack.stackTagCompound != null) {
			stack.stackTagCompound.removeTag(NBT_UUID_KEY);
		}
	}

	@Override
	public String getInvName() {
		return stack.getDisplayName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		// usually the ItemStack is in the player's inventory
		return true;
	}

	/**
	 * saves this inventory to the ItemStack.
	 */
	protected final void saveData() {
		writeToNbt(getNbt());
	}

	private NBTTagCompound getNbt() {
		return ItemStacks.getNbt(stack, nbtKey);
	}

}
