package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Network;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * @author diesieben07
 */
@ChannelHandler.Sharable
public final class SCMessageHandlerClient extends SCMessageHandler {

    public static final SCMessageHandlerClient INSTANCE = new SCMessageHandlerClient();

    @Override
    @SideOnly(Side.CLIENT)
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof InternalPacket) {
            ((InternalPacket) msg)._sc$internal$receiveDirect(Network.CLIENT, getMinecraft().getConnection().getNetworkManager());
        } else if (msg instanceof SPacketCustomPayload) {
            SPacketCustomPayload packet = (SPacketCustomPayload) msg;
            if (!NetworkImpl.handleCustomPayload(packet.getChannelName(), packet.getBufferData(), Network.CLIENT, getMinecraft().getConnection().getNetworkManager())) {
                ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

}
