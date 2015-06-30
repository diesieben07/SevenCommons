package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.util.concurrent.CompletableFuture;

/**
 * @author diesieben07
 */
public final class ResponseNettyVersion<R extends Packet.Response> implements BaseNettyPacket {

    private final Packet.WithResponse<R> original;
    private final CompletableFuture<R> future;

    public ResponseNettyVersion(Packet.WithResponse<R> original, CompletableFuture<R> future) {
        this.original = original;
        this.future = future;
    }

    @Override
    public void _sc$handle(EntityPlayer player) {
        SimplePacketData.WithResponse<Packet.WithResponse<R>, R> data = PacketToChannelMap.getData(original);
        try {
            future.complete(data.handler.apply(original, player));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }

    @Override
    public net.minecraft.network.Packet _sc$encodeToPlayer(EntityPlayerMP player) {
        SimplePacketData.WithResponse<Packet.WithResponse<R>, R> data = PacketToChannelMap.getData(original);
        return new S3FPacketCustomPayload(data.channel, encodeToBytes(data));
    }

    @Override
    public net.minecraft.network.Packet _sc$encode() {
        SimplePacketData.WithResponse<Packet.WithResponse<R>, R> data = PacketToChannelMap.getData(original);
        return SevenCommons.proxy.makeC17Packet(data.channel, encodeToBytes(data));
    }

    private byte[] encodeToBytes(SimplePacketData.WithResponse<Packet.WithResponse<R>, R> data) {
        int uniqueId = ResponseSupport.nextID();
        ResponseSupport.register(uniqueId, future);


        MCDataOutput out = Network.newOutput(original.expectedSize() + 2);
        out.writeByte(data.packetID);
        out.writeByte(uniqueId);

        original.writeTo(out);

        return out.toByteArray();
    }
}
