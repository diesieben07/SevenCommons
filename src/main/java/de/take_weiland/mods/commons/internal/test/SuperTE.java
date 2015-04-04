package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.tileentity.TileEntity;

/**
 * @author diesieben07
 */
public class SuperTE extends TileEntity {

    @ToNbt
    private String toNbtTest;


}
