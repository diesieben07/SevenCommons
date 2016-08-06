package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketConstructor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Base class for handlers that have a response of some form</p>
 */
abstract class ResponseHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends SimpleChannelBuilderImpl.Handler {

    final PacketConstructor<P> constructor;
    final PacketConstructor<R> responseConstructor;

    ResponseHandler(byte characteristics, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor) {
        super(characteristics);
        this.constructor = constructor;
        this.responseConstructor = responseConstructor;
    }

    @Override
    public final void accept(String channel, int packetID, MCDataInput in, byte side, NetworkManager manager) {
        int uniqueID = in.readUnsignedByte();

        if ((uniqueID & ResponseSupport.IS_RESPONSE) == 0) {
            P packet;
            try {
                packet = constructor.newInstance(in);
            } catch (Exception x) {
                throw wrapConstructException(packetID, x);
            }
            if ((characteristics & Network.ASYNC) == 0) {
                NetworkImpl.getScheduler(side).execute(() -> {
                    doHandle(manager, NetworkImpl.getPlayer(side, manager), packetID, channel, uniqueID, packet);
                    return false;
                });
            } else {
                doHandle(manager, NetworkImpl.getPlayer(side, manager), packetID, channel, uniqueID, packet);
            }
        } else {
            CompletableFuture<Packet.Response> future = ResponseSupport.unregister(uniqueID);
            try {
                future.complete(responseConstructor.newInstance(in));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    abstract void doHandle(NetworkManager manager, EntityPlayer player, int packetID, String channel, int responseID, P packet);

    abstract Object handler();

    @Override
    public String toString() {
        return String.format("Response packet handler (handler=%s, constructor=%s, responseConstructor=%s", handler(), constructor, responseConstructor);
    }

    protected static <R extends Packet.Response> void sendResponse(R response, NetworkManager manager, int packetID, int responseID, String channel) {
        NetworkImpl.sendPacket(new WrappedResponsePacket<>(response, packetID, responseID, channel), manager);
    }
}
