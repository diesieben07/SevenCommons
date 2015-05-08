package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.AbstractSyncer;
import de.take_weiland.mods.commons.sync.PropertyAccess;
import net.minecraft.block.Block;

/**
 * @author diesieben07
 */
final class BlockSyncer extends AbstractSyncer.ForImmutable<Block> {

    public <OBJ> BlockSyncer(OBJ obj, PropertyAccess<OBJ, Block> property) {
        super(obj, property);
    }

    @Override
    public void encode(Block block, MCDataOutput out) {
        out.writeBlock(block);
    }

    @Override
    protected Block decode(MCDataInput in) {
        return in.readBlock();
    }
}
