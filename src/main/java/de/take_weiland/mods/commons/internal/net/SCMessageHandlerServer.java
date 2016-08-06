package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Network;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * @author diesieben07
 */
public final class SCMessageHandlerServer extends SCMessageHandler {

    private final NetworkManager manager;

    public SCMessageHandlerServer(NetworkManager manager) {
        this.manager = manager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof InternalPacket) {
            ((InternalPacket) msg)._sc$internal$receiveDirect(Network.SERVER, manager);
        } else if (msg instanceof CPacketCustomPayload) {
            CPacketCustomPayload packet = (CPacketCustomPayload) msg;
            if (!NetworkImpl.handleCustomPayload(packet.getChannelName(), packet.getBufferData(), Network.SERVER, manager)) {
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
