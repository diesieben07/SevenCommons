package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
public interface RawPacket extends BaseNettyPacket {

    void handle(EntityPlayer player);

    Packet encodeToPlayer(EntityPlayerMP player);

    Packet encode();

    @Override
    default void _sc$handle(EntityPlayer player) {
        handle(player);
    }

    @Override
    default Packet _sc$encodeToPlayer(EntityPlayerMP player) {
        return encodeToPlayer(player);
    }

    @Override
    default Packet _sc$encode() {
        return encode();
    }

    interface UsingCustomPayload extends BaseNettyPacket {

        void handle(EntityPlayer player);

        byte[] write();

        default byte[] writeToPlayer(EntityPlayerMP player) {
            return write();
        }

        String channel();

        @Override
        default void _sc$handle(EntityPlayer player) {
            handle(player);
        }

        @Override
        default Packet _sc$encodeToPlayer(EntityPlayerMP player) {
            return new S3FPacketCustomPayload(channel(), writeToPlayer(player));
        }

        @Override
        default Packet _sc$encode() {
            return SevenCommons.proxy.makeC17Packet(channel(), write());
        }
    }

}
