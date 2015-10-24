package de.take_weiland.mods.commons.client;

import net.minecraft.inventory.Slot;

/**
 * <p>Implement this on a {@link net.minecraft.client.gui.inventory.GuiContainer} and you will receive callbacks for {@code Slot}
 * draw calls.</p>
 *
 * @author diesieben07
 */
public interface SlotDrawHooks {

    /**
     * <p>Called before a Slot is drawn. Return false to cancel drawing.</p>
     *
     * @param slot the Slot
     * @return false to cancel drawing
     */
    default boolean preDraw(Slot slot) {
        return true;
    }

    /**
     * <p>Called after a Slot is drawn.</p>
     *
     * @param slot the Slot
     */
    default void postDraw(Slot slot) {
    }

}
