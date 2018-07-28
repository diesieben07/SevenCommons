package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.SevenCommons
import de.takeweiland.mods.commons.net.registry.SimplePacketData
import de.takeweiland.mods.commons.net.registry.getResponsePacketData
import de.takeweiland.mods.commons.netbase.BasicCustomPayloadPacket
import de.takeweiland.mods.commons.netbase.CustomPayloadHandler
import de.takeweiland.mods.commons.scheduler.mainThread
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.relauncher.Side

internal class PacketBasePayloadHandler(private val name: String, private val byId: Array<SimplePacketData<*>?>) : CustomPayloadHandler {

    private fun getAnyPacketData(id: Int): SimplePacketData<*> {
        return byId.getOrNull(id) ?: throw IllegalArgumentException("Unknown packet id $id received for channel $name")
    }

    override fun handle(channel: String, buf: ByteBuf, side: Side, player: EntityPlayer?): Boolean {
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
                    val packet: AnyPacketWithResponse<*> = data.factory(buf)
                    launch(side.mainThread) {
                        val actualPlayer = player.obtainForNetworkOnMainThread()
                        val response = if (packet is PacketWithResponse<*>) {
                            packet.process(actualPlayer)
                        } else {
                            (packet as PacketWithResponse.Async<*>).process(actualPlayer).await()
                        }
                        response.responseId = responseId and RESPONSE_ID_MASK
                        val ch = ((actualPlayer as? EntityPlayerMP)?.connection?.netManager ?: SevenCommons.proxy.clientToServerNetworkManager).channel()
                        ch.writeAndFlush(response, ch.voidPromise())
                    }
                }
            }
        }
        return true
    }

    internal abstract class BaseWithResponseWrapper<R : ResponsePacket, P : AnyPacketWithResponse<R>>(
        protected val packet: P, protected val deferred: CompletableDeferred<R>
    ) : BasicCustomPayloadPacket {

        override fun <R> serialize(factory: (channel: String, buf: ByteBuf) -> R): R {
            // we are being sent over the network, reserve a tracking ID for the response
            // then store the Deferred under that ID
            val data = getResponsePacketData(packet.javaClass)
            val responseId = reservePacketResponse(deferred)
            val buf = Unpooled.buffer(packet.expectedSize + 2)
            buf.writeByte(data.id)
            buf.writeByte(responseId)
            packet.write(buf)
            return factory(data.channel, buf)
        }

        override fun handle(player: EntityPlayer?, side: Side) {
            launch(side.mainThread) {
                handle(player.obtainForNetworkOnMainThread())
            }
        }

        abstract suspend fun handle(player: EntityPlayer)

    }

    internal class WithResponseWrapper<R : ResponsePacket>(packet: PacketWithResponse<R>, deferred: CompletableDeferred<R>) :
        BaseWithResponseWrapper<R, PacketWithResponse<R>>(packet, deferred) {

        override suspend fun handle(player: EntityPlayer) {
            deferred.complete(packet.process(player))
        }
    }

    internal class WithResponseWrapperAsync<R : ResponsePacket>(packet: PacketWithResponse.Async<R>, deferred: CompletableDeferred<R>) :
        BaseWithResponseWrapper<R, PacketWithResponse.Async<R>>(packet, deferred) {

        override suspend fun handle(player: EntityPlayer) {
            val result = packet.process(player)
            deferred.complete(result.await())
        }

    }
}