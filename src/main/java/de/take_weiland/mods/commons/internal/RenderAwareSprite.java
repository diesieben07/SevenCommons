package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;

/**
 * @author diesieben07
 */
@SideOnly(Side.CLIENT)
public interface RenderAwareSprite extends IIcon {

    IIcon preRender(RenderBlocks rb, int side);

    void postRender(RenderBlocks rb, int side);

}
