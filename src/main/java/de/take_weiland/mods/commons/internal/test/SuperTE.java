package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.tileentity.TileEntityInventory;

/**
 * @author diesieben07
 */
public class SuperTE extends TileEntityInventory {

    @Override
    public String getDefaultName() {
        return null;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }
}
