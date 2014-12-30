package de.take_weiland.mods.commons.internal.syncimpl;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
public enum ItemStackWatcher implements Watcher<ItemStack> {
	VALUE {
		@Override
		public void read(MCDataInput in, SyncableProperty<ItemStack> property) {
			property.set(in.readItemStack());
		}
	},
	CONTENTS {
		@Override
		public void read(MCDataInput in, SyncableProperty<ItemStack> property) {
			ItemStack val = property.get();
			val.itemID = in.readShort();
			val.setItemDamage(in.readShort());
			val.stackSize = in.readByte();
			val.stackTagCompound = in.readNBT();
		}
	};

	@Override
	public void setup(SyncableProperty<ItemStack> property) {

	}

	@Override
	public boolean hasChanged(SyncableProperty<ItemStack> property) {
		return !ItemStacks.equal(property.get(), (ItemStack) property.getData());
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<ItemStack> property) {
		ItemStack val = property.get();
		out.writeItemStack(val);
		property.setData(ItemStacks.clone(val));
	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<ItemStack> property) {
		out.writeItemStack(property.get());
	}

}
