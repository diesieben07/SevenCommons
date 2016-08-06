package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.internal.PacketContainerButton;
import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <p>Implement this on your Container to make it applicable to handling GUI Buttons.</p>
 * <p>Call {@link #triggerButton(int)} on the client to trigger the button
 * on both client and server.</p>
 */
public interface ButtonContainer {

    /**
     * <p>Called when a button is clicked.</p>
     *
     * @param side     the logical side
     * @param player   the player
     * @param buttonId the button
     */
    void onButtonClick(Side side, EntityPlayer player, int buttonId);

    /**
     * <p>Trigger the given button. This method will call {@link #onButtonClick(Side, EntityPlayer, int)}
     * on both client and server.</p>
     * <p>This method must only be called from the client thread and will throw a {@code ClassCastException} if this interface
     * was implemented on a class that does not extend {@link Container}.</p>
     *
     * @param button the button
     */
    default void triggerButton(int button) {
        onButtonClick(Side.CLIENT, SevenCommons.proxy.getClientPlayer(), button);
        new PacketContainerButton(((Container) this).windowId, button).sendToServer();
    }

}
