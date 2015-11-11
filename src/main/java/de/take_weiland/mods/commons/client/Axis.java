package de.take_weiland.mods.commons.client;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author diesieben07
 */
public enum Axis {

    Y,
    Z,
    X;

    public static Axis get(ForgeDirection dir) {
        return values()[dir.ordinal() >> 1];
    }

}
