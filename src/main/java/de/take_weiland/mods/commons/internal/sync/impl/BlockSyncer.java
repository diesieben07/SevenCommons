package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import net.minecraft.block.Block;

/**
 * @author diesieben07
 */
public final class BlockSyncer implements ValueSyncer<Block>, HandleSubclasses {

	public BlockSyncer() { }

	@Override
	public boolean hasChanged(Block value, Object data) {
		return value != data;
	}

	@Override
	public Object writeAndUpdate(Block value, MCDataOutputStream out, Object data) {
		out.writeBlock(value);
		return value;
	}

	@Override
	public Block read(MCDataInputStream in, Object data) {
		return in.readBlock();
	}
}
