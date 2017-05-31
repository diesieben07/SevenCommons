package de.take_weiland.mods.commons.net.packet.raw

import net.minecraft.network.NetworkManager

interface ReceivingNettyAwarePacket : NettyAwarePacket, NettyAsyncReceive {

    override fun getLocalReceive(): NettyAsyncReceive = this

    override fun sendTo(manager: NetworkManager) {
        manager.channel().writeAndFlush(this)
    }
}