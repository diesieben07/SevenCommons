package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import net.minecraft.block.Block;

/**
 * @author diesieben07
 */
public class BlockSyncer implements ValueSyncer<Block>, HandleSubclasses {

	private Block companion;

	@Override
	public boolean hasChanged(Block value) {
		return value != companion;
	}

	@Override
	public void writeAndUpdate(Block value, MCDataOutputStream out) {
		out.writeBlock(value);
		companion = value;
	}

	@Override
	public Block read(MCDataInputStream in) {
		return in.readBlock();
	}
}
