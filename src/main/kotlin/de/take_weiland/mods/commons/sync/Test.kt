package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.sync.impl.sync
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable

/**
 * @author diesieben07
 */

open class Test : TileEntity(), ITickable {

    open var bla: Int by sync(0)
    open var blubb: Boolean by sync(false)

    override fun update() {
        bla++
    }
}

class Foo : Test() {

    var fuzzy: Char by sync('0')

    override var bla: Int
        get() = 2
        set(value) { }

}

fun bla(name: String, age: Int = 30) {
    println("$name is $age")
}

fun main(args: Array<String>) {
    bla(name = "hello")
}