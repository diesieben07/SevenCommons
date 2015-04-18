package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface PacketHandler<P extends Packet> extends BasePacketHandler<P> {

    void onReceive(P packet, EntityPlayer player, Side side);

    @Override
    default Packet receive0(P packet, EntityPlayer player, Side side) {
        onReceive(packet, player, side);
        return null;
    }

    @FunctionalInterface
    interface WithResponse<P extends Packet.WithResponse<R>, R extends Packet> extends BasePacketHandler<P> {

        R onReceive(P packet, EntityPlayer player, Side side);

        @Override
        default Packet receive0(P packet, EntityPlayer player, Side side) {
            return onReceive(packet, player, side);
        }
    }

}
