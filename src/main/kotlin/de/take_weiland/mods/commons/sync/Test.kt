package de.take_weiland.mods.commons.sync

import net.minecraft.tileentity.TileEntity

/**
 * @author diesieben07
 */
class Test : TileEntity() {

    var bla: String? by sync()

}