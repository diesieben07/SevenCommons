package de.take_weiland.mods.commons.sync

/**
 * @author diesieben07
 */
typealias ChangedPropertyStore = HashMap<Any?, Array<Any?>>

operator fun ChangedPropertyStore.set(obj: Any?, key: Int, value: Any?) {
    var arr = get(obj)
    if (arr == null) {
        arr = arrayOfNulls(key + 1)
        put(obj, arr)
    } else if (arr.size <= key) {
        arr = arr.copyOf(key + 1)
        put(obj, arr)
    }

    arr[key] = value
}

operator fun ChangedPropertyStore.get(obj: Any?, key: Int): Any? {
    val arr = get(obj)
    return if (arr != null && arr.size > key) {
        arr[key]
    } else {
        null
    }
}

inline fun Any?.isMaskedNull(): Boolean = this === MaskedNull

inline fun Any?.maskNull(): Any {
    return this ?: MaskedNull
}

inline fun Any?.unmaskNull(): Any? {
    return if (this === MaskedNull) null else this
}

object MaskedNull
