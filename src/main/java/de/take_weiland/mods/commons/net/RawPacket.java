package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
public interface RawPacket {

    void handle(EntityPlayer player);

    net.minecraft.network.Packet encodeToPlayer(EntityPlayerMP player);

    net.minecraft.network.Packet encode();

    interface UsingCustomPayload extends RawPacket {

        String channel();

        @Override
        default Packet encodeToPlayer(EntityPlayerMP player) {
            return new S3FPacketCustomPayload(channel(), writeToPlayer(player));
        }

        @Override
        default Packet encode() {
            return new C17PacketCustomPayload(channel(), write());
        }

        default byte[] writeToPlayer(EntityPlayerMP player) {
            return write();
        }

        byte[] write();
    }

}
