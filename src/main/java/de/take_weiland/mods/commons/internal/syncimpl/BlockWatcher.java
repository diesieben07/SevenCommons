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
	public <OBJ> void setup(SyncableProperty<Block, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<Block, OBJ> property, OBJ instance) {
		out.writeBlock(property.get(instance));
	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<Block, OBJ> property, OBJ instance) {
		return property.get(instance) != property.getData(instance);
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<Block, OBJ> property, OBJ instance) {
		Block block = property.get(instance);
		out.writeBlock(block);
		property.setData(block, instance);
	}

	@Override
	public <OBJ> void read(MCDataInput in, SyncableProperty<Block, OBJ> property, OBJ instance) {
		property.set(in.readBlock(), instance);
	}
}
