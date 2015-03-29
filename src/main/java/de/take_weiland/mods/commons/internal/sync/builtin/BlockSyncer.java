package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.block.Block;

/**
 * @author diesieben07
 */
final class BlockSyncer implements Syncer<Block, Block> {

    @Override
    public Class<Block> getCompanionType() {
        return Block.class;
    }

    @Override
    public boolean equal(Block value, Block companion) {
        return value == companion;
    }

    @Override
    public Block writeAndUpdate(Block value, Block companion, MCDataOutput out) {
        out.writeBlock(value);
        return value;
    }

    @Override
    public Block read(Block value, Block companion, MCDataInput in) {
        return in.readBlock();
    }
}
