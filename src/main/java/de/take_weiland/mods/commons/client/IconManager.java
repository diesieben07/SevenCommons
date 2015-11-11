package de.take_weiland.mods.commons.client;

import net.minecraft.util.IIcon;

/**
 * <p></p>
 *
 * @author diesieben07
 */
public interface IconManager {

    IIcon getIcon(int side, int meta);

    int getMeta(int front, int frontRotation);

}
