package de.takeweiland.mods.commons.net.base

import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Take Weiland
 */
interface NetworkChannel<M : NetworkSerializable> {

    val name: String

    fun receive(buf: ByteBuf, player: EntityPlayer)

    fun receive(message: M, player: EntityPlayer)

    fun encode(message: M, buf: ByteBuf)

}