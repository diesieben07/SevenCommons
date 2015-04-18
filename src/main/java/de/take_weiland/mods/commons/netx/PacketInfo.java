package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * @author diesieben07
 */
class PacketInfo<P extends Packet> {

    final PacketConstructor<P> constructor;
    private final BasePacketHandler<P> handler;
    final int id;

    PacketInfo(PacketConstructor<P> constructor, BasePacketHandler<P> handler, int id) {
        this.constructor = constructor;
        this.handler = handler;
        this.id = id;
    }

    Packet invokeReceive(ByteBuf buf, EntityPlayer player, Side side) {
        return handler.receive0(constructor.apply(buf), player, side);
    }

    static final class WithResponseHandling<P extends Packet.WithResponse<R>, R extends Packet> extends PacketInfo<P> {

        private final PacketConstructor<R> responseConstructor;
        private byte nextId;
        private final TByteObjectMap<CompletableFuture<R>> callbacks = new TByteObjectHashMap<>();

        WithResponseHandling(int id, PacketConstructor<P> constructor, BasePacketHandler<P> handler, PacketConstructor<R> responseConstructor) {
            super(constructor, handler, id);
            this.responseConstructor = responseConstructor;
        }

        byte registerResponseWaiter(CompletableFuture<R> future) {
            byte id = nextId++;
            callbacks.put(id, future);
            return id;
        }

        @Override
        Packet invokeReceive(ByteBuf buf, EntityPlayer player, Side side) {
            byte responseID = buf.readByte();
            CompletableFuture<R> future = callbacks.remove((byte) (int) responseID);
            if (future != null) {
                future.complete(responseConstructor.apply(buf));
            }
            return null;
        }

    }

}
