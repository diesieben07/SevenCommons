package de.take_weiland.mods.commons.netx;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.relauncher.Side;
import gnu.trove.map.TByteObjectMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author diesieben07
 */
final class NetworkChannelImpl implements NetworkChannel {

    private final ImmutableMap<Class<? extends Packet>, PacketInfo<?>> packetToInfo;
    private final TByteObjectMap<PacketInfo> idToInfo;
    private final String channel;
    private final EntityPlayerMP serverPlayer;

    private byte nextResponseId = 0;


    NetworkChannelImpl(String channel, ImmutableMap<Class<? extends Packet>, PacketInfo<?>> packetToInfo, TByteObjectMap<PacketInfo> idToInfo, EntityPlayerMP serverPlayer) {
        this.packetToInfo = packetToInfo;
        this.idToInfo = idToInfo;
        this.channel = channel;
        this.serverPlayer = serverPlayer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof S3FPacketCustomPayload) {
            S3FPacketCustomPayload mcPacket = (S3FPacketCustomPayload) msg;
            if (mcPacket.func_149169_c().equals(channel)) {
                ByteBuf buf = Unpooled.wrappedBuffer(mcPacket.func_149168_d());

                byte packetId = buf.readByte();

                PacketInfo<?> packetInfo = idToInfo.get(packetId);
                Packet response = packetInfo.invokeReceive(buf, )

                return;
            }
        }

        ctx.fireChannelRead(msg);
    }

    void sendToServer(Packet packet) {
        ByteBuf buf = makeBuffer(packet);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C17PacketCustomPayload(channel, buf));
    }

    <P extends Packet.WithResponse<R>, R extends Packet> CompletionStage<R> sendToServer(P packet) {
        CompletableFuture<R> future = new CompletableFuture<>();
        ByteBuf buf = makeBufferWithResponse(packet, future);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C17PacketCustomPayload(channel, buf));

        return future;
    }

    void sendToPlayer(Packet packet, EntityPlayerMP player) {
        ByteBuf buf = makeBuffer(packet);
        player.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload(channel, buf));
    }

    <P extends Packet.WithResponse<R>, R extends Packet> CompletionStage<R> sendToPlayer(P packet, EntityPlayerMP player) {
        ByteBuf buf = makeBuffer(packet);

    }

    @NotNull
    private <P extends Packet.WithResponse<R>, R extends Packet> ByteBuf makeBufferWithResponse(P packet, CompletableFuture<R> future) {
        ByteBuf buf = Unpooled.buffer(packet.expectedSize() + 2);
        @SuppressWarnings("unchecked")
        PacketInfo.WithResponseHandling<P, R> info = (PacketInfo.WithResponseHandling<P, R>) packetToInfo.get(packet.getClass());

        buf.writeByte(info.id);
        buf.writeByte(info.registerResponseWaiter(future));

        packet.write(buf);
        return buf;
    }

    private ByteBuf makeBuffer(Packet packet) {
        ByteBuf buf = Unpooled.buffer(packet.expectedSize() + 1);

        buf.writeByte(packetToInfo.get(packet.getClass()).id);
        packet.write(buf);
        return buf;
    }


    void clientMessage(ByteBuf buf) {
        byte packetId = buf.readByte();
        PacketInfo<?> packetInfo = idToInfo.get(packetId);
        Packet response = packetInfo.invokeReceive(buf, Minecraft.getMinecraft().thePlayer, Side.CLIENT);
        if (response != null) {
            sendToServer(response);
        }
    }

    void serverMessage(ByteBuf buf, EntityPlayerMP player) {
        byte packetId = buf.readByte();
        PacketInfo<?> packetInfo = idToInfo.get(packetId);
        Packet response = packetInfo.invokeReceive(buf, player, Side.SERVER);
        if (response != null) {
            sendToPlayer(response, player);
        }
    }

}
