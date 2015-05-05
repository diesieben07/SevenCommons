package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.block.Block;

/**
 * @author diesieben07
 */
enum BlockSyncer implements Syncer.ForImmutable<Block> {

    INSTANCE;

    @Override
    public Class<Block> getCompanionType() {
        return Block.class;
    }

    @Override
    public void write(Block value, MCDataOutput out) {
        out.writeBlock(value);
    }

    @Override
    public Block read(MCDataInput in) {
        return in.readBlock();
    }
}
