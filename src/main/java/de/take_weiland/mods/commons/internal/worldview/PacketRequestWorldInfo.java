package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

/**
 * @author diesieben07
 */
public class PacketRequestWorldInfo implements Packet.WithResponse<PacketWorldInfo> {

    private final int dimension;

    public PacketRequestWorldInfo(int dimension) {
        this.dimension = dimension;
    }

    public PacketRequestWorldInfo(MCDataInput in) {
        this.dimension = in.readInt();
    }

    @Override
    public void writeTo(MCDataOutput out) throws Exception {
        out.writeInt(dimension);
    }

    public PacketWorldInfo handle() {
        WorldServer world = DimensionManager.getWorld(dimension);
        if (world == null) {
            // todo
            return new PacketWorldInfo(BlockPos.ORIGIN, 0, 0);
        } else {
            BlockPos spawn = world.getSpawnPoint();
            return new PacketWorldInfo(spawn, world.getSkylightSubtracted(), world.getWorldTime());
        }
    }
}
