package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.net.codec.writeVarInt
import de.takeweiland.mods.commons.net.registry.getPlainPacketData
import de.takeweiland.mods.commons.netbase.BasicCustomPayloadPacket
import de.takeweiland.mods.commons.netbase.SimplePacket
import de.takeweiland.mods.commons.scheduler.mainThread
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlinx.coroutines.experimental.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
abstract class Packet : PacketBase(), BasicCustomPayloadPacket, SimplePacket {

    abstract fun process(player: EntityPlayer)

    final override fun sendTo(network: NetworkManager) {
        val channel = network.channel()
        channel.writeAndFlush(this, channel.voidPromise())
    }

    override fun <R> serialize(factory: (channel: String, buf: ByteBuf) -> R): R {
        val data = getPlainPacketData(javaClass)
        val buf = Unpooled.buffer(expectedSize + 1)
        buf.writeVarInt(data.id)
        write(buf)
        return factory(data.channel, buf)
    }

    final override fun handle(player: EntityPlayer?, side: Side) {
        launch(side.mainThread) {
            process(player.obtainForNetworkOnMainThread())
        }
    }
}