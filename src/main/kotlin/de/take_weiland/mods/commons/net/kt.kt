package de.take_weiland.mods.commons.net

/**
 * @author diesieben07
 */
inline fun test(provider: () -> (() -> String)): String {
    return provider()()
}

val s: String = "hello"

fun main(args: Array<String>) {
    println(test { ::s })
}