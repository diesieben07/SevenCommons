package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.sync.impl.sync
import de.take_weiland.mods.commons.util.isServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable

/**
 * @author diesieben07
 */

open class Test : TileEntity(), ITickable {

    open var bla: Int by sync(0)
    open var blubb: Boolean by sync(false)

    override fun update() {
        if (isServer) bla++

    }
}

class Foo : Test() {

}