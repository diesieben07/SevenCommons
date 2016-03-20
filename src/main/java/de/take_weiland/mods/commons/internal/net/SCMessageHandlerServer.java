package de.take_weiland.mods.commons.internal.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.ProtocolException;
import de.take_weiland.mods.commons.util.Scheduler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * @author diesieben07
 */
public final class SCMessageHandlerServer extends SCMessageHandler {

    private final EntityPlayerMP player;

    public SCMessageHandlerServer(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BaseNettyPacket) {
            BaseNettyPacket packet = (BaseNettyPacket) msg;
            byte props = packet._sc$characteristics();

            if ((props & Network.SERVER) == 0) {
                throw new ProtocolException("Packet " + msg + " received on invalid side server");
            }

            if ((props & Network.ASYNC) != 0) {
                packet._sc$handle(player);
            } else {
                Scheduler.server().execute(new SyncPacketExecServer(packet, this.player));
            }
        } else if (!(msg instanceof C17PacketCustomPayload) || !NetworkImpl.handleServerCustomPacket((C17PacketCustomPayload) msg, player, this)) {
            ctx.fireChannelRead(msg);
        }
    }

    private static final class SyncPacketExecServer implements Scheduler.Task {

        private final BaseNettyPacket packet;
        private final EntityPlayerMP player;

        SyncPacketExecServer(BaseNettyPacket packet, EntityPlayerMP player) {
            this.packet = packet;
            this.player = player;
        }

        @Override
        public boolean execute() {
            packet._sc$handle(player);
            return false;
        }

        @Override
        public String toString() {
            return String.format("Server thread packet exec (packet=%s, player=%s)", packet, player);
        }
    }

    @Override
    Side side() {
        return Side.SERVER;
    }
}
