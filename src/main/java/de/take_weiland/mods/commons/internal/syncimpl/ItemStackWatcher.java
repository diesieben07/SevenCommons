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
		public <OBJ> void read(MCDataInput in, SyncableProperty<ItemStack, OBJ> property, OBJ instance) {
			property.set(in.readItemStack(), instance);
		}
	},
	CONTENTS {
		@Override
		public <OBJ> void read(MCDataInput in, SyncableProperty<ItemStack, OBJ> property, OBJ instance) {
			ItemStack val = property.get(instance);
			val.itemID = in.readShort();
			val.setItemDamage(in.readShort());
			val.stackSize = in.readByte();
			val.stackTagCompound = in.readNBT();
		}
	};

	@Override
	public <OBJ> void setup(SyncableProperty<ItemStack, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<ItemStack, OBJ> property, OBJ instance) {
		return !ItemStacks.equal(property.get(instance), (ItemStack) property.getData(instance));
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<ItemStack, OBJ> property, OBJ instance) {
		ItemStack val = property.get(instance);
		out.writeItemStack(val);
		property.setData(ItemStacks.clone(val), instance);
	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<ItemStack, OBJ> property, OBJ instance) {
		out.writeItemStack(property.get(instance));
	}

}
