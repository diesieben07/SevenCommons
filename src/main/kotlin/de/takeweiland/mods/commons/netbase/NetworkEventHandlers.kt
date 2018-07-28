@file:Mod.EventBusSubscriber
package de.takeweiland.mods.commons.netbase

import de.takeweiland.mods.commons.netbase.handler.*
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.network.NetHandlerPlayServer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * @author Take Weiland
 */
@SubscribeEvent
@SideOnly(Side.CLIENT)
internal fun clientConnectedToServer(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
    val nh = event.handler as NetHandlerPlayClient
    val channel = nh.networkManager.channel()

    if (event.isLocal) {
        channel.pipeline().addBefore(VANILLA_PACKET_HANDLER, SC_INBOUND_HANDLER, ClientInboundDirectPayloadHandler)
    } else {
        channel.pipeline().addBefore(VANILLA_PACKET_HANDLER, SC_INBOUND_HANDLER, ClientInboundCustomPayloadHandler)
        channel.pipeline().addAfter(VANILLA_PACKET_HANDLER, SC_OUTBOUND_HANDLER, ClientOutboundCustomPayloadEncoder)
    }
}

@SubscribeEvent
internal fun serverConnectionFromClient(event: FMLNetworkEvent.ServerConnectionFromClientEvent) {
    val nh = event.handler as NetHandlerPlayServer
    val channel = nh.netManager.channel()

    if (event.isLocal) {
        channel.pipeline().addBefore(VANILLA_PACKET_HANDLER, SC_INBOUND_HANDLER, ServerInboundDirectPayloadHandler(nh.player))
    } else {
        channel.pipeline().addBefore(VANILLA_PACKET_HANDLER, SC_INBOUND_HANDLER, ServerInboundCustomPayloadHandler(nh.player))
        channel.pipeline().addAfter(VANILLA_PACKET_HANDLER, SC_OUTBOUND_HANDLER, ServerOutboundCustomPayloadEncoder)
    }
}

private const val VANILLA_PACKET_HANDLER = "packet_handler"
private const val SC_INBOUND_HANDLER = "sevencommons:inbound_handler"
private const val SC_OUTBOUND_HANDLER = "sevencommons:outbound_handler"