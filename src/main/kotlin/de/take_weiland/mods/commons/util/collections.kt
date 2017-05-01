package de.take_weiland.mods.commons.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet

/**
 * @author diesieben07
 */
inline fun <T> List<T>.fastForEach(body: (T) -> Unit) {
    val end = size
    var i = 0
    while (i < end) body(this[i++])
}


inline fun <T> immutableSetOf() = ImmutableSet.of<T>()
inline fun <T> immutableSetOf(e1: T) = ImmutableSet.of(e1)
inline fun <T> immutableSetOf(e1: T, e2: T) = ImmutableSet.of(e1, e2)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T) = ImmutableSet.of(e1, e2, e3)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T, e4: T) = ImmutableSet.of(e1, e2, e3, e4)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T) = ImmutableSet.of(e1, e2, e3, e4, e5)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, vararg others: T) = ImmutableSet.of(e1, e2, e3, e4, e5, e6, others)

inline fun <T> immutableListOf() = ImmutableList.of<T>()
inline fun <T> immutableListOf(e1: T) = ImmutableList.of(e1)
inline fun <T> immutableListOf(e1: T, e2: T) = ImmutableList.of(e1, e2)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T) = ImmutableList.of(e1, e2, e3)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T) = ImmutableList.of(e1, e2, e3, e4)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T) = ImmutableList.of(e1, e2, e3, e4, e5)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T) = ImmutableList.of(e1, e2, e3, e4, e5, e6)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T) = ImmutableList.of(e1, e2, e3, e4, e5, e6, e7)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T) = ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T) = ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T, e10: T) = ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T, e10: T, e11: T) = ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11)
inline fun <T> immutableListOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, e7: T, e8: T, e9: T, e10: T, e11: T, e12: T, vararg others: T) = ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, others)
