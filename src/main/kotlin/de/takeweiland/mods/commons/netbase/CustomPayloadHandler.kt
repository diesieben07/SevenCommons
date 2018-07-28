package de.takeweiland.mods.commons.netbase

import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
interface CustomPayloadHandler {

    fun handle(
        channel: String, buf: ByteBuf, side: Side, player: EntityPlayer?
    ): Boolean

}