package de.takeweiland.mods.commons.net2

import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
interface CustomPayloadChannel {

    fun receive(side: Side, player: EntityPlayer?, channel: String, data: ByteBuf)


}