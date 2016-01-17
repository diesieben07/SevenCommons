package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class PacketRequestView implements Packet {

    private final int chunkX, chunkZ, radius;

    public PacketRequestView(int chunkX, int chunkZ, int radius) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.radius = radius;
    }

    public PacketRequestView(MCDataInput in) {
        this.chunkX = in.readInt();
        this.chunkZ = in.readInt();
        this.radius = in.readInt();
    }

    @Override
    public void writeTo(MCDataOutput out) throws Exception {
        out.writeInt(chunkX);
        out.writeInt(chunkZ);
        out.writeInt(radius);
    }

    public void handle(EntityPlayer player) {
        // todo radius??
        ServerChunkViewManager.addView(player, player.dimension, chunkX, chunkZ);
    }
}
