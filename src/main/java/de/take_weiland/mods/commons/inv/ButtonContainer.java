package de.take_weiland.mods.commons.inv;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>Implement this on your Container to make it applicable to handling GUI Buttons.</p>
 * <p>Call {@link de.take_weiland.mods.commons.inv.Containers#triggerButton(int)} on the client to trigger the button
 * on both client and server.</p>
 */
public interface ButtonContainer {

    void onButtonClick(Side side, EntityPlayer player, int buttonId);

}
