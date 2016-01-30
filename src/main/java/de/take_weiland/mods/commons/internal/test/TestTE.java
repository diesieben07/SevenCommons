package de.take_weiland.mods.commons.internal.test;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * @author diesieben07
 */
public class TestTE extends SuperTE {

    private boolean first = false;

    @Override
    public void updateEntity() {
        if (sideOf(this).isServer() && !first) {
            first = true;

            ForgeChunkManager.forceChunk(ForgeChunkManager.requestTicket(testmod_sc.instance, worldObj, ForgeChunkManager.Type.NORMAL), new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));
        }
    }

}
