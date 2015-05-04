package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.block.Block;

import java.util.function.Consumer;

/**
 * @author diesieben07
 */
enum BlockSyncer implements Syncer.Simple<Block, Block> {

    INSTANCE;

    @Override
    public Class<Block> getCompanionType() {
        return Block.class;
    }

    @Override
    public <T_OBJ> Change<Block> checkChange(T_OBJ obj, Block value, Block companion, Consumer<Block> companionSetter) {
        if (value == companion) {
            return noChange();
        } else {
            companionSetter.accept(value);
            return newValue(value);
        }
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
