package de.take_weiland.mods.commons.inv

import de.take_weiland.mods.commons.internal.PacketContainerButton
import de.take_weiland.mods.commons.internal.SevenCommons
import de.take_weiland.mods.commons.net.simple.sendToServer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraftforge.fml.relauncher.Side

/**
 *
 * Implement this on your Container to make it applicable to handling GUI Buttons.
 *
 * Call [triggerButton] on the client to trigger the button
 * on both client and server.
 */
interface ButtonContainer {

    /**
     *
     * Called when a button is clicked.

     * @param side     the logical side
     * *
     * @param player   the player
     * *
     * @param buttonId the button
     */
    fun onButtonClick(side: Side, player: EntityPlayer, buttonId: Int)

    /**
     *
     * Trigger the given button. This method will call [onButtonClick]
     * on both client and server.
     *
     * This method must only be called from the client thread and will throw a `ClassCastException` if this interface
     * was implemented on a class that does not extend [Container].

     * @param button the button
     */
    fun triggerButton(button: Int) {
        onButtonClick(Side.CLIENT, SevenCommons.proxy.clientPlayer, button)
        PacketContainerButton((this as Container).windowId, button).sendToServer()
    }

}
