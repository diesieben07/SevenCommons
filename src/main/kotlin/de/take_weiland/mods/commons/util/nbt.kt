package de.take_weiland.mods.commons.util

import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagList

/**
 * @author diesieben07
 */
inline fun NBTTagList.forEach(action: (NBTBase) -> Unit) {
    this.forEach<NBTBase> { action(it) }
}

@JvmName("forEachTyped")
inline fun <reified T : NBTBase> NBTTagList.forEach(action: (T) -> Unit) {
    this.tagCount().let {
        var i = 0
        while (i < it) {
            this[i].let {
                if (it is T) action(it) else throw IllegalStateException("NBTTagList contains unexpected tag $it, expected ${T::class.java}")
            }
            i++
        }
    }
}

inline operator fun NBTTagList.plusAssign(tag: NBTBase) = this.appendTag(tag)
inline operator fun NBTTagList.iterator() = NBTTagListIterator(this)

class NBTTagListIterator(private val list: NBTTagList) {

    private var index = 0

    operator fun hasNext() = index < list.tagCount()

    operator fun next() = list[index++]

}