package de.take_weiland.mods.commons.internal.syncimpl;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import net.minecraft.block.Block;

/**
 * @author diesieben07
 */
public enum BlockWatcher implements Watcher<Block> {
	INSTANCE;

	@Override
	public void setup(SyncableProperty<Block> property) {

	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<Block> property) {
		out.writeBlock(property.get());
	}

	@Override
	public boolean hasChanged(SyncableProperty<Block> property) {
		return property.get() != property.getData();
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<Block> property) {
		Block block = property.get();
		out.writeBlock(block);
		property.setData(block);
	}

	@Override
	public void read(MCDataInput in, SyncableProperty<Block> property) {
		property.set(in.readBlock());
	}
}
