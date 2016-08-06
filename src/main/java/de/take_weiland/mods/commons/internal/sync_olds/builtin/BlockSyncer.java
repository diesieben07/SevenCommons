package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.block.Block;

/**
 * @author diesieben07
 */
enum BlockSyncer implements TypeSyncer.ForImmutable<Block> {

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
