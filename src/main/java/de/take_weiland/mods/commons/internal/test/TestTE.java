package de.take_weiland.mods.commons.internal.test;

import net.minecraftforge.common.ForgeChunkManager;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * @author diesieben07
 */
public class TestTE extends SuperTE {

    private boolean first = false;

    @Override
    public void update() {
        if (sideOf(this).isServer() && !first) {
            first = true;

        }
    }

}
