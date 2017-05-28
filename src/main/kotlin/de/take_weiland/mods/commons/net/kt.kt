package de.take_weiland.mods.commons.net

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * @author diesieben07
 */
inline fun test(provider: () -> (() -> String)): String {
    return provider()()
}

class Bla {

    companion object {

        @JvmStatic
        fun String.foo(): String {
            return this.toLowerCase()
        }

    }

}
class A {

    var a: Int = 5

}

class B(a: A) {

    var b: Int by a::a

}

operator fun <T> KProperty0<T>.getValue(obj: Any, property: KProperty<*>): T {
    return get()
}

operator fun <T> KMutableProperty0<T>.setValue(obj: Any, property: KProperty<*>, value: T): Unit {
    set(value)
}

val s: String = "hello"

fun main(args: Array<String>) {
    var a = 3
    val b = 3
    when (++a > b) {
        true -> println("yes")
        false -> println("false")
    }
}