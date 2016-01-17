package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;

/**
 * @author diesieben07
 */
public class PacketWorldInfo implements Packet.Response {

    public final int spawnX, spawnY, spawnZ;
    public final int  skylightSubtracted;
    public final long worldTime;

    public PacketWorldInfo(int spawnX, int spawnY, int spawnZ, int skylightSubtracted, long worldTime) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.skylightSubtracted = skylightSubtracted;
        this.worldTime = worldTime;
    }

    public PacketWorldInfo(MCDataInput in) {
        spawnX = in.readInt();
        spawnY = in.readUnsignedByte();
        spawnZ = in.readInt();
        skylightSubtracted = in.readInt();
        worldTime = in.readLong();
    }

    @Override
    public void writeTo(MCDataOutput out) throws Exception {
        out.writeInt(spawnX);
        out.writeByte(spawnY);
        out.writeInt(spawnZ);
        out.writeInt(skylightSubtracted);
        out.writeLong(worldTime);
    }

}
