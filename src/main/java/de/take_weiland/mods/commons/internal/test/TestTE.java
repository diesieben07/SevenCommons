package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.util.Sides;

import java.util.Random;

/**
 * @author diesieben07
 */
public class TestTE extends SuperTE implements SyncedInterface {

    @Sync
    @ToNbt
    public String test = "__nullval__";

    @Sync
    Integer foobarusMax;

    @Sync
    @ToNbt
    float syncFoobar;

    private int tick;

    @Override
    public String getSomeData() {
        return test;
    }

    @Override
    public void setSomeData(String i) {
        test = i;
    }

    @Override
    public void updateEntity() {
        if (tick++ % 10 == 0) {
            if (Sides.sideOf(this).isServer()) {
                test = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
                syncFoobar = new Random().nextFloat();
            } else {
                System.out.println("client val's are test=" + test + ", syncFoobar=" + syncFoobar);
            }
        }
    }
}
