package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.SevenCommons
import de.takeweiland.mods.commons.net.base.AsyncNetworkChannel
import de.takeweiland.mods.commons.net.base.NetworkSerializable
import de.takeweiland.mods.commons.net.registry.SimplePacketData
import de.takeweiland.mods.commons.net.registry.getResponsePacketData
import de.takeweiland.mods.commons.scheduler.mainThread
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.relauncher.Side

internal class PacketBaseNetworkChannel(override val name: String, private val byId: Array<SimplePacketData<*>?>) : AsyncNetworkChannel<NetworkSerializable> {

    private fun getAnyPacketData(id: Int): SimplePacketData<*> {
        return byId.getOrNull(id) ?: throw IllegalArgumentException("Unknown packet id $id received for channel $name")
    }

    internal class WithResponseWrapper<R : ResponsePacket>(private val packet: PacketWithResponse<R>, private val deferred: CompletableDeferred<R>) :
        NetworkSerializable {

        override val channel: String
            get() = getResponsePacketData(packet.javaClass).channel

        override fun getPacket(packetFactory: (String, ByteBuf) -> AnyMCPacket): AnyMCPacket {
            // we are being sent over the network, reserve a tracking ID for the response
            // then store the Deferred under that ID
            val data = getResponsePacketData(packet.javaClass)
            val responseId = reservePacketResponse(deferred)
            val buf = Unpooled.buffer(packet.expectedSize + 2)
            buf.writeByte(data.id)
            buf.writeByte(responseId)
            packet.write(buf)
            return packetFactory(data.channel, buf)
        }

        fun handle(player: EntityPlayer) {
            deferred.complete(packet.process(player))
        }
    }

    override fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?) {
        val id = buf.readUnsignedByte().toInt()
        val data = getAnyPacketData(id)
        when (data) {
            is SimplePacketData.Plain<*> -> {
                val packet: Packet = data.factory(buf)
                launch(side.mainThread) {
                    packet.process(player.obtainForNetworkOnMainThread())
                }
            }
            is SimplePacketData.WithResponse<*, *> -> {
                val responseId = buf.readUnsignedByte().toInt()
                if ((responseId and RESPONSE_MARKER_BIT) != 0) {
                    val response = data.responseFactory(buf)
                    getPacketResponseFutureAndRemove(responseId and RESPONSE_ID_MASK).complete(response)
                } else {
                    val packet: PacketWithResponse<*> = data.factory(buf)
                    launch(side.mainThread) {
                        val actualPlayer = player.obtainForNetworkOnMainThread()
                        val response = packet.process(actualPlayer)
                        response.responseId = responseId and RESPONSE_ID_MASK
                        val ch = ((actualPlayer as? EntityPlayerMP)?.connection?.netManager ?: SevenCommons.proxy.clientToServerNetworkManager).channel()
                        ch.writeAndFlush(response, ch.voidPromise())
                    }
                }
            }
        }
    }

    override fun receive(message: NetworkSerializable, side: Side, player: EntityPlayer?) {
        launch(side.mainThread) {
            when (message) {
                is Packet -> message.process(player.obtainForNetworkOnMainThread())
                is WithResponseWrapper<*> -> message.handle(player.obtainForNetworkOnMainThread())
                is ResponsePacket -> TODO()
            }
        }
    }

}