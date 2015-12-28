package de.take_weiland.mods.commons.internal.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.ProtocolException;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Scheduler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
@ChannelHandler.Sharable
public final class SCMessageHandlerClient extends SCMessageHandler {

    public static final SCMessageHandlerClient INSTANCE = new SCMessageHandlerClient();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BaseNettyPacket) {
            BaseNettyPacket packet = (BaseNettyPacket) msg;
            byte props = packet._sc$characteristics();

            if ((props & Network.CLIENT) == 0) {
                throw new ProtocolException("Packet " + msg + " received on invalid side client");
            }

            if ((props & Network.ASYNC) != 0) {
                packet._sc$handle(Players.getClient());
            } else {
                SchedulerInternalTask.add(Scheduler.client(), new SyncPacketExecClient(packet));
            }
        } else if (!(msg instanceof S3FPacketCustomPayload) || !NetworkImpl.handleClientCustomPacket((S3FPacketCustomPayload) msg, this)) {
            ctx.fireChannelRead(msg);
        }
    }

    private static final class SyncPacketExecClient extends SchedulerInternalTask {

        private final BaseNettyPacket packet;

        SyncPacketExecClient(BaseNettyPacket packet) {
            this.packet = packet;
        }

        @Override
        public boolean execute() {
            packet._sc$handle(Players.getClient());
            return false;
        }

        @Override
        public String toString() {
            return String.format("Client thread packet exec (packet=%s)", packet);
        }
    }

    @Override
    Side side() {
        return Side.CLIENT;
    }

}
