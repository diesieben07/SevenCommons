package de.take_weiland.mods.commons.net

interface Property<T : Comparable<T>>

fun main(args: Array<String>) {
    val property: Property<Any> = object : Property<String>
    val value = parsePropertyValue(property, "test")
}

fun <T : Comparable<T>> parsePropertyValue(property: Property<T>, value: String): T = TODO()
