package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * @author diesieben07
 */
public final class WithResponseRequestNettyVersion<R extends Packet.Response> implements BaseNettyPacket {

    private final Packet.WithResponse<R> original;
    private final CompletableFuture<R> future;

    public WithResponseRequestNettyVersion(Packet.WithResponse<R> original, CompletableFuture<R> future) {
        this.original = original;
        this.future = future;
    }

    @Override
    public void _sc$handle(EntityPlayer player) {
        SimplePacketData.WithResponse<Packet.WithResponse<R>, R> data = PacketToChannelMap.getData(original);
        data.completeFuture(future, original, player);
    }

    @Override
    public byte[] _sc$encode() {
        int uniqueId = ResponseSupport.nextID();
        ResponseSupport.register(uniqueId, future);

        MCDataOutput out = Network.newOutput(original.expectedSize() + 2);
        out.writeByte(PacketToChannelMap.getData(original).packetID);
        out.writeByte(uniqueId);

        original.writeTo(out);

        return out.toByteArray();
    }

    @Override
    public String _sc$channel() {
        return PacketToChannelMap.getData(original).channel;
    }

    @Override
    public boolean _sc$async() {
        return PacketToChannelMap.getData(original).async;
    }
}