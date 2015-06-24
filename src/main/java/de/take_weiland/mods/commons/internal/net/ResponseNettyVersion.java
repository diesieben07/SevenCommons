package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public final class ResponseNettyVersion<R extends Packet> implements BaseNettyPacket {

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
        return makePacket(S3FPacketCustomPayload::new);
    }

    @Override
    public net.minecraft.network.Packet _sc$encode() {
        return makePacket(SevenCommons.proxy.getC17PacketCstr());
    }

    private net.minecraft.network.Packet makePacket(BiFunction<String, byte[], ? extends net.minecraft.network.Packet> cstr) {
        SimplePacketData.WithResponse<Packet.WithResponse<R>, R> data = PacketToChannelMap.getData(original);
        int uniqueId = data.nextID();
        if (data.futures.putIfAbsent(uniqueId, future) != null) {
            throw new RuntimeException("Duplicate future ID!");
        }

        MCDataOutput out = Network.newOutput(original.expectedSize() + 2);
        out.writeByte(data.packetID);
        out.writeByte(uniqueId);

        original.writeTo(out);

        return cstr.apply(data.channel, out.toByteArray());
    }
}
