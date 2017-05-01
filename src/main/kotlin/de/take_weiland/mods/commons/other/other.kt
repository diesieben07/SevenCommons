package de.take_weiland.mods.commons.other

import de.take_weiland.mods.commons.util.forEach
import de.take_weiland.mods.commons.util.plusAssign
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString

/**
 * @author diesieben07
 */
fun main(args: Array<String>) {
    val nbt = NBTTagList()
    nbt += NBTTagString("hello")
    nbt.forEach { it : String -> println(it) }
}