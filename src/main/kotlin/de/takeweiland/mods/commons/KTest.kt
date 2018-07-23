package de.takeweiland.mods.commons

/**
 * @author Take Weiland
 */
interface Foo {
    fun bar()
}

fun main(args: Array<String>) {
    val x = object : Foo {
        override fun bar() {
            if (true) return
        }
    }
}

fun userPrompt(callback: (Boolean) -> Unit) {
}
