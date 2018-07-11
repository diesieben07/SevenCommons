package de.takeweiland.mods.commons.net.codec

import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.ByteBufUtils

fun ByteBuf.writeVarInt(v: Int) {
    // TODO
    ByteBufUtils.writeVarInt(this, v, 5)
}

fun ByteBuf.readVarInt(): Int = ByteBufUtils.readVarInt(this, 5) // TODO