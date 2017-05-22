package de.take_weiland.mods.commons.util

import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString

/**
 * @author diesieben07
 */
inline fun NBTTagList.forEach(action: (NBTBase) -> Unit) {
    this.forEach<NBTBase> { action(it) }
}

@JvmName("forEachTyped")
inline fun <reified T : NBTBase> NBTTagList.forEach(action: (T) -> Unit) {
    tagCount().let {
        var i = 0
        while (i < it) {
            this[i].let {
                if (it is T) action(it) else throw IllegalStateException("NBTTagList contains unexpected tag $it, expected ${T::class.java}")
            }
            i++
        }
    }
}

@JvmName("forEachString")
inline fun NBTTagList.forEach(action: (String) -> Unit) {
    forEach<NBTTagString> { action(it.string) }
}

operator fun NBTTagList.plusAssign(tag: NBTBase) = appendTag(tag)
operator fun NBTTagList.iterator(): MutableIterator<NBTBase> = tagList.iterator()

operator fun NBTTagList.plusAssign(value: String) {
    appendTag(NBTTagString(value))
}

inline fun NBTTagCompound.forEach(crossinline action: (String, NBTBase) -> Unit) {
    tagMap.forEach({ k, v -> action(k, v) })
}
