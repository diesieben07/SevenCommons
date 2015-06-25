package de.take_weiland.mods.commons.internal.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
public interface BaseNettyPacket {

    void _sc$handle(EntityPlayer player);

    net.minecraft.network.Packet _sc$encodeToPlayer(EntityPlayerMP player);

    net.minecraft.network.Packet _sc$encode();

    interface UsingCustomPayload extends BaseNettyPacket {

        byte[] _sc$write();

        default byte[] _sc$writeToPlayer(EntityPlayerMP player) {
            return _sc$write();
        }

        String _sc$channel();

        @Override
        default Packet _sc$encode() {
            return new C17PacketCustomPayload(_sc$channel(), _sc$write());
        }

        @Override
        default Packet _sc$encodeToPlayer(EntityPlayerMP player) {
            return new S3FPacketCustomPayload(_sc$channel(), _sc$writeToPlayer(player));
        }
    }

}
