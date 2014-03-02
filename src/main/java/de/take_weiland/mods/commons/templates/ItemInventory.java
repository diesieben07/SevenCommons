package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.util.ItemStacks;
import de.take_weiland.mods.commons.util.NBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ItemInventory<T extends ItemInventory<T>> extends AbstractInventory<T> {

	private static final String DEFAULT_NBT_KEY = "inventory";
	
	protected final ItemStack originalStack;
	protected final String nbtKey;
	
	protected ItemInventory(ItemStack item, String nbtKey) {
		super();
		originalStack = item;
		this.nbtKey = nbtKey;
		readFromNbt(getNbt());
	}
	
	protected ItemInventory(ItemStack item) {
		this(item, DEFAULT_NBT_KEY);
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void onChange() {
		super.onChange();
		saveData();
	}

	private NBTTagCompound getNbt() {
		return NBT.getOrCreateCompound(ItemStacks.getNbt(originalStack), nbtKey);
	}
	
	protected void saveData() {
		writeToNbt(getNbt());
	}
	
	public abstract static class WithPlayer<T extends WithPlayer<T>> extends ItemInventory<T> {

		protected final int hotbarIndex;
		protected final EntityPlayer player;
		
		protected WithPlayer(EntityPlayer player, String nbtKey) {
			super(checkNotNull(checkNotNull(player, "Player must not be null!").getCurrentEquippedItem(), "Player needs to have an Item equipped!"), nbtKey);
			this.player = player;
			hotbarIndex = player.inventory.currentItem;
		}
		
		protected WithPlayer(EntityPlayer player) {
			this(player, DEFAULT_NBT_KEY);
		}
		
		public final int getHotbarIndex() {
			return hotbarIndex;
		}
		
		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return player == this.player;
		}

		protected void saveData() {
			super.saveData();
			player.setCurrentItemOrArmor(0, originalStack);
		}
		
	}

}
