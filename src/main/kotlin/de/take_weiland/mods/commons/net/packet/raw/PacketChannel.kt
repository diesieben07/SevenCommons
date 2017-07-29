package de.take_weiland.mods.commons.net.packet.raw

import com.google.common.collect.ImmutableMap
import de.take_weiland.mods.commons.net.packet.DefaultPacketChannelBuilderImpl
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.LoaderState
import java.util.concurrent.ConcurrentHashMap

/**
 * A receiver for custom payload packets.
 *
 * @author diesieben07
 */
interface PacketChannel {

    /**
     * The channel to receive packets from.
     */
    val channel: String

    /**
     * Called on the netty thread when a custom payload packet is received on this channel.
     *
     * @param buf the payload of the packet
     * @param player the player receiving the packet, may be `null` on the client
     */
    fun receive(buf: ByteBuf, player: EntityPlayer?)

    /**
     * Whether to automatically release the buffer after [receive] has returned. If this property is set to `false`,
     * `receive` must call [ByteBuf.release] on the buffer.
     */
    val autoRelease: Boolean get() = true

    companion object {

        inline operator fun invoke(channelName: String, body: DefaultPacketChannelBuilder.() -> Unit): PacketChannel {
            return builder(channelName).also(body).build()
        }

        fun builder(channelName: String): DefaultPacketChannelBuilder {
            return DefaultPacketChannelBuilderImpl(channelName)
        }

        fun register(channel: PacketChannel) {
            val lastValue = globalPacketChannelMap.putIfAbsent(channel.channel, channel)
            if (lastValue != null) {
                throw IllegalArgumentException("Duplicate packet channel ${channel.channel}")
            }
        }
    }

}

internal fun getPacketChannel(channelName: String): PacketChannel? {
    return FrozenChannelMapHolder.frozenPacketChannelMap[channelName]
}

private val globalPacketChannelMap = ConcurrentHashMap<String, PacketChannel>()

internal object FrozenChannelMapHolder {

    @JvmStatic
    val frozenPacketChannelMap: ImmutableMap<String, PacketChannel> = run {
        require(Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
        ImmutableMap.copyOf(globalPacketChannelMap)
    }

}