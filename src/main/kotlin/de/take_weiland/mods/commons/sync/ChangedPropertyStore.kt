package de.take_weiland.mods.commons.sync

/**
 * @author diesieben07
 */

internal class ChangedPropertyStore<V>  {

    var map = arrayOfNulls<Any?>(5)

    fun get(key: Int): V? {
        return if (key < map.size) unmaskNull(map[key]) else null
    }

    fun put(key: Int, value: V) {
        if (key >= map.size) {
            map = map.copyOf(key + 1)
        }
        map[key] = maskNull(value)
    }

    inline fun forEach(body: (Int, V) -> Unit) {
        map.let { map ->
            for (i in map.indices) {
                val v = map[i]
                if (v != null) body(i, unmaskNull(v))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun unmaskNull(value: Any?): V = (if (value === Companion) null else value) as V
    private fun maskNull(value: V): Any? = value ?: Companion

    companion object

}

