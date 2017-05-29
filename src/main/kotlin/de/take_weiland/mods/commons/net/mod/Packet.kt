package de.take_weiland.mods.commons.net.mod

import de.take_weiland.mods.commons.net.simple.SimplePacket
import de.take_weiland.mods.commons.util.thread
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

interface AsyncReceive {

    fun receiveAsync(player: EntityPlayer)

}

interface Packet : BasePacket, SimplePacket, AsyncReceive {

    override fun receiveAsync(player: EntityPlayer) {
        player.thread.run { receive(player) }
    }

    fun receive(player: EntityPlayer)

    override fun sendTo(manager: NetworkManager) {
        manager.channel().writeAndFlush(this)
    }

    interface Async : Packet {

        override fun receiveAsync(player: EntityPlayer) {
            receive(player)
        }

    }

    interface WithResponse<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

        fun receive(player: EntityPlayer) : R

        override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
            return WrappedPacketWithResponse(this).also { manager.channel().writeAndFlush(it) }
        }

        interface Async<out R : Response> : WithResponse<R> {

            override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
                return WrappedPacketWithResponseAsync(this).also { manager.channel().writeAndFlush(it) }
            }

        }

    }

    interface WithAsyncResponse<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

        fun receive(player: EntityPlayer) : CompletionStage<out R>

        override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
            return WrappedPacketWithAsyncResponse(this).also { manager.channel().writeAndFlush(it) }
        }

        interface Async<out R : Response> : WithAsyncResponse<R> {

            override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
                return WrappedPacketWithAsyncResponseAsync(this).also { manager.channel().writeAndFlush(it) }
            }
        }

    }

    interface Response : BasePacket

}