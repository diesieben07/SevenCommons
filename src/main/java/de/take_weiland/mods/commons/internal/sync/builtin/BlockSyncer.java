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
    public Block decode(MCDataInput in) {
        return in.readBlock();
    }

    @Override
    public void encode(Block block, MCDataOutput out) {
        out.writeBlock(block);
    }

    @Override
    public Class<Block> companionType() {
        return Block.class;
    }
}
