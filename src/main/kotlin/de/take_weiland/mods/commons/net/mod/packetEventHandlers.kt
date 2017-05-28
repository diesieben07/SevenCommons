@file:Mod.EventBusSubscriber

/**
 * @author diesieben07
 */
package de.take_weiland.mods.commons.net.mod

import de.take_weiland.mods.commons.util.clientPlayer
import de.take_weiland.mods.commons.util.clientThread
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SubscribeEvent
internal fun clientConnectionToServer(event: FMLNetworkEvent.ClientConnectedToServerEvent) {

}

internal interface NettyChannelDirectExecutable {

    fun executeOnNetty(player: EntityPlayer)

}

private abstract class SCMessageHandler : ChannelInboundHandlerAdapter()

private object SCMessageHanderClient : SCMessageHandler() {

    @SideOnly(Side.CLIENT)
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        when (msg) {
            is Packet.Async -> msg.receive(clientPlayer)
            is Packet -> clientThread { msg.receive(clientPlayer) }
            is WrappedPacketWithResponse<*,*> -> msg.executePacketAndComplete(clientPlayer)
            else -> super.channelRead(ctx, msg)
        }
    }
}