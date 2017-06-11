package de.take_weiland.mods.commons.sync

import net.minecraft.tileentity.TileEntity

/**
 * @author diesieben07
 */

open class Test : TileEntity() {

    var bla: Int by sync { int(0) }
////    open var blubb: Boolean by sync(false)
//
//    override fun update() {
////        if (isServer) bla++
//
//    }
}

class Foo : Test() {

}