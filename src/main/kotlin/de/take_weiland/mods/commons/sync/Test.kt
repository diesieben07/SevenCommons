package de.take_weiland.mods.commons.sync

import net.minecraft.tileentity.TileEntity

/**
 * @author diesieben07
 */

open class Test : TileEntity(), SyncEnabled {

    var bla: Int by sync(0)
}

class Foo : Test() {

}