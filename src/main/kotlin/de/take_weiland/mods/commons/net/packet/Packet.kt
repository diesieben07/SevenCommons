package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.simple.SimplePacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import java.util.concurrent.CompletionStage

/**
 * @author diesieben07
 */
const val defaultExpectedPacketSize = 32

interface PacketReader<out T : BasePacket> {

    fun ByteBuf.read(): T

}

interface BasePacket {

    fun ByteBuf.write()

    val expectedSize: Int get() = defaultExpectedPacketSize

}

interface BasePacketNoResponse : BasePacket, CustomPayloadPacket, SimplePacket {

    @Deprecated(message = "internal", level = DeprecationLevel.HIDDEN)
    override fun writePayload(buf: ByteBuf) {
        buf.writePacketId(data.packetId)
        buf.write()
    }

    @Deprecated(message = "internal", level = DeprecationLevel.HIDDEN)
    override val expectedPayloadSize: Int
        get() = expectedSize + 1

    @Deprecated(message = "internal", level = DeprecationLevel.HIDDEN)
    override val channel: String
        get() = data.channel

    override fun sendTo(manager: NetworkManager) {
        manager.channel().writeAndFlush(this)
    }

}

interface Packet : BasePacketNoResponse {

    fun receive(player: EntityPlayer)

    interface Async : BasePacketNoResponse {

        fun receive(player: EntityPlayer?)

        @Deprecated(message = "internal", level = DeprecationLevel.HIDDEN)
        override fun receiveAsync(player: EntityPlayer?) {
            receive(player)
        }

    }

    interface WithResponse<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

        fun receive(player: EntityPlayer): R

        interface Async<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

            fun receive(player: EntityPlayer?): R

            override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
                return WrappedPacketWithResponseAsync(this, manager).also { manager.channel().writeAndFlush(it) }
            }
        }

        override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
            return WrappedPacketWithResponse(this, manager).also { manager.channel().writeAndFlush(it) }
        }

    }

    interface WithAsyncResponse<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

        fun receive(player: EntityPlayer): CompletionStage<out R>

        override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
            return WrappedPacketWithAsyncResponse(this, manager).also { manager.channel().writeAndFlush(it) }
        }

        interface Async<out R : Response> : BasePacket, SimplePacket.WithResponse<R> {

            fun receive(player: EntityPlayer?): CompletionStage<out R>

            override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
                return WrappedPacketWithAsyncResponseAsync(this, manager).also { manager.channel().writeAndFlush(it) }
            }
        }

    }

    interface Response : BasePacket

    // Internal Methods

    @Deprecated(message = "internal", level = DeprecationLevel.HIDDEN)
    override fun receiveAsync(player: EntityPlayer?) {
        player.runPacketOnThread(this::receive)
    }

}