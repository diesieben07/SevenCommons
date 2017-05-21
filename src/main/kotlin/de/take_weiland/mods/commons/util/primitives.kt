package de.take_weiland.mods.commons.util

/**
 * @author diesieben07
 */
inline fun Float.toIntBits() = java.lang.Float.floatToRawIntBits(this)
inline fun Int.toFloatBits() = java.lang.Float.intBitsToFloat(this)
inline fun Double.toLongBits() = java.lang.Double.doubleToLongBits(this)
inline fun Long.toDoubleBits() = java.lang.Double.longBitsToDouble(this)

inline fun Byte.toUnsignedInt() = toInt() and 0xFF
