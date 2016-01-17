package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Sync;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * @author diesieben07
 */
public class TestTE extends SuperTE implements SyncedInterface {

    private float syncFoobar;

    @Sync
    @ToNbt
    public int rotMeta;

    private int lastRot = -1;

    @Sync(inContainer = true)
    public float getSync() {
        return syncFoobar;
    }

    private void setSync(float f) {
        syncFoobar = f;
    }

    @Override
    public void updateEntity() {
        if (sideOf(this).isClient()) {
            System.out.println("tick!");
        }
    }

}
