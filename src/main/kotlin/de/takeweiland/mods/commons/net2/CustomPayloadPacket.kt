package de.takeweiland.mods.commons.net2

import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
interface CustomPayloadPacket {

    val expectedSize: Int

    val channel: String

    fun write(buf: ByteBuf)

    fun handle(player: EntityPlayer?, side: Side)

}