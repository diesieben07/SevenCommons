package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.util.math.BlockPos;

/**
 * @author diesieben07
 */
public class PacketWorldInfo implements Packet.Response {

    public final BlockPos spawn;
    public final int  skylightSubtracted;
    public final long worldTime;

    public PacketWorldInfo(BlockPos spawn, int skylightSubtracted, long worldTime) {
        this.spawn = spawn;
        this.skylightSubtracted = skylightSubtracted;
        this.worldTime = worldTime;
    }

    public PacketWorldInfo(MCDataInput in) {
        spawn = in.readBlockPos();
        skylightSubtracted = in.readInt();
        worldTime = in.readLong();
    }

    @Override
    public void writeTo(MCDataOutput out) throws Exception {
        out.writeBlockPos(spawn);
        out.writeInt(skylightSubtracted);
        out.writeLong(worldTime);
    }

}
