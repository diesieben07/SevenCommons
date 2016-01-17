package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;

/**
 * @author diesieben07
 */
public class PacketBlockChange implements Packet {

    public final int x, y, z;
    public final int dimension;
    public final int data;

    public PacketBlockChange(int dimension, int x, int y, int z, int block, int metadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.data = (block << 4) | metadata;
    }

    public PacketBlockChange(MCDataInput in) throws Exception {
        this.x = in.readInt();
        this.y = in.readUnsignedByte();
        this.z = in.readInt();
        this.dimension = in.readVarInt();
        this.data = in.readUnsignedShort();
    }

    @Override
    public void writeTo(MCDataOutput out) throws Exception {
        out.writeInt(x);
        out.writeByte(y);
        out.writeInt(z);
        out.writeVarInt(dimension);
        out.writeShort(data);
    }

}
