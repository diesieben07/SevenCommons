package de.take_weiland.mods.commons.net.mod

import de.take_weiland.mods.commons.net.simple.SimplePacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import java.util.concurrent.CompletionStage

/**
 * @author diesieben07
 */
interface BasePacket {

    fun ByteBuf.writeTo()

    val expectedSize: Int get() = 32

}

interface Packet : BasePacket, SimplePacket {

    fun receive(player: EntityPlayer)

    override fun sendTo(manager: NetworkManager) {
        manager.channel().writeAndFlush(this)
    }

    interface Async : Packet

    interface WithResponse<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

        fun receive(player: EntityPlayer) : R

        override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
            return WrappedPacketWithResponse(this).also { manager.channel().writeAndFlush(it) }
        }

        interface Async<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

            fun receive(player: EntityPlayer) : CompletionStage<out R>

            override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
                return WrappedPacketWithResponse
            }

        }

    }

    interface Response : BasePacket

}