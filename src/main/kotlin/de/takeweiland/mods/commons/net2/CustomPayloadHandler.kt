package de.takeweiland.mods.commons.net2

import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
interface CustomPayloadHandler {

    fun handle(channel: String, buf: ByteBuf, side: Side)

}