package de.take_weiland.mods.commons.client;

import net.minecraft.util.IIcon;

/**
 * <p></p>
 *
 * @author diesieben07
 */
public interface IconManager {

    IIcon getIcon(int meta, BlockFace face);

    int getMeta(BlockFace front, int frontRotation);

}
