package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.packet.raw.ReceivingNettyAwarePacket
import de.take_weiland.mods.commons.net.simple.SimplePacket
import de.take_weiland.mods.commons.util.thread
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import java.util.concurrent.CompletionStage

/**
 * @author diesieben07
 */
const val defaultExpectedPacketSize = 32

interface BasePacket {

    fun ByteBuf.write()

    val expectedSize: Int get() = defaultExpectedPacketSize

}

interface ReceivablePacket : BasePacket, ReceivingNettyAwarePacket {



}

interface Packet : ReceivablePacket, SimplePacket {

    fun receive(player: EntityPlayer)

    override fun writeForRemote(buf: ByteBuf) {
        buf.writeByte(data.packetId)
        buf.write()
    }

    override val expectedSize: Int
        get() = super.expectedSize

    override fun receiveAsync(player: EntityPlayer) {
        player.thread.run { receive(player) }
    }

    override val channel: String
        get() = data.channel

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