package de.take_weiland.mods.commons;

import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.tileentity.TileEntity;

/**
* @author diesieben07
*/
public abstract class SuperTE extends TileEntity {

    @Sync
    float syncFoobar;


}
