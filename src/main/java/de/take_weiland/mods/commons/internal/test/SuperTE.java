package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.tileentity.TileEntityInventory;

/**
 * @author diesieben07
 */
public class SuperTE extends TileEntityInventory {

    private String toNbtTest;

    @Override
    public String getDefaultName() {
        return "test";
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }
}
