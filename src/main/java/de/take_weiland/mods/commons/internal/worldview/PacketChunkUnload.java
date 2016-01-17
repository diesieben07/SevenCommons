package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;

/**
 * @author diesieben07
 */
public class PacketChunkUnload implements Packet {

    public final int dimension, chunkX, chunkZ;

    public PacketChunkUnload(int dimension, int chunkX, int chunkZ) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public PacketChunkUnload(MCDataInput in) {
        this.dimension = in.readVarInt();
        this.chunkX = in.readInt();
        this.chunkZ = in.readInt();
    }

    @Override
    public void writeTo(MCDataOutput out) throws Exception {
        out.writeVarInt(dimension);
        out.writeInt(chunkX);
        out.writeInt(chunkZ);
    }
}
