package de.take_weiland.mods.commons.templates;

import static com.google.common.base.Preconditions.checkNotNull;
import de.take_weiland.mods.commons.util.Inventories;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class ItemInventory extends AbstractInventory {

	private static final String DEFAULT_NBT_KEY = "inventory";
	
	protected final ItemStack originalStack;
	protected final String nbtKey;
	
	protected ItemInventory(ItemStack item, String nbtKey) {
		originalStack = item;
		this.nbtKey = nbtKey;
		Inventories.readInventory(this, ItemStacks.getNbt(item).getTagList(nbtKey));
	}
	
	protected ItemInventory(ItemStack item) {
		this(item, DEFAULT_NBT_KEY);
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}
	
	@Override
	public void closeChest() {
		originalStack.stackTagCompound.setTag(nbtKey, Inventories.writeInventory(this));
	}
	
	public abstract static class WithPlayer extends ItemInventory {

		protected final EntityPlayer player;
		
		protected WithPlayer(EntityPlayer player, String nbtKey) {
			super(getStack(player), nbtKey);
			this.player = player;
		}
		
		protected WithPlayer(EntityPlayer player) {
			this(player, DEFAULT_NBT_KEY);
		}
		
		private static ItemStack getStack(EntityPlayer player) {
			checkNotNull(player, "Player must not be null!");
			ItemStack stack = player.getCurrentEquippedItem();
			checkNotNull(stack, "Player needs to have an Item equipped!");
			return stack;
		}
		
		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return player == this.player;
		}
		
	}

}
