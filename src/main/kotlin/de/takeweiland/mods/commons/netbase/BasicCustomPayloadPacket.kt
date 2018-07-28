package de.takeweiland.mods.commons.netbase

import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
interface BasicCustomPayloadPacket : SimplePacket {

    fun handle(player: EntityPlayer?, side: Side)

    fun <R> serialize(factory: (channel: String, buf: ByteBuf) -> R): R

    override fun sendTo(network: NetworkManager) {
        val ch = network.channel()
        ch.write(this, ch.voidPromise())
    }

}