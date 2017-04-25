package de.take_weiland.mods.commons.util

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet

/**
 * @author diesieben07
 */
inline fun <T> immutableSetOf() = ImmutableSet.of<T>()
inline fun <T> immutableSetOf(e1: T) = ImmutableSet.of(e1)
inline fun <T> immutableSetOf(e1: T, e2: T) = ImmutableSet.of(e1, e2)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T) = ImmutableSet.of(e1, e2, e3)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T, e4: T) = ImmutableSet.of(e1, e2, e3, e4)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T) = ImmutableSet.of(e1, e2, e3, e4, e5)
inline fun <T> immutableSetOf(e1: T, e2: T, e3: T, e4: T, e5: T, e6: T, vararg others: T) = ImmutableSet.of(e1, e2, e3, e4, e5, e6, others)

typealias ImmutableMapBuilder<K, V> = ImmutableMap.Builder<K, V>

inline infix fun <K, V> K.xto(value: V): Unit { this.put(key, value) }

inline fun <K, V> immutableMapOf(builder: ImmutableMap.Builder<K, V>.() -> Unit): ImmutableMap<K, V> = ImmutableMap.builder<K, V>().also(builder).build()

