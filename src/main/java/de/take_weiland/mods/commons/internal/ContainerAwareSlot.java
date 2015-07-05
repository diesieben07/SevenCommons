package de.take_weiland.mods.commons.internal;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * @author diesieben07
 */
public interface ContainerAwareSlot {

    /**
     * Called from {@link ASMHooks#onSlotAdded(Container, Slot)}
     *
     * @param container the container
     */
    void _sc$injectContainer(Container container);

}
