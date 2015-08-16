package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.util.Listenable;

import java.util.Random;

/**
 * @author diesieben07
 */
public class TestTE extends SuperTE implements SyncedInterface, Listenable<String> {

    //    @Sync
    public String test = "__nullval__";

    //    @Sync
    Integer foobarusMax;

    private float syncFoobar;

    @Sync
    private float getSync() {
        return syncFoobar;
    }

    private void setSync(float f) {
        syncFoobar = f;
    }

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
        if (!worldObj.isRemote) {
            syncFoobar = new Random().nextFloat();
        }
//        if (tick++ % 10 == 0) {
//            if (Sides.sideOf(this).isServer()) {
//                test = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
//                syncFoobar = new Random().nextFloat();
//            } else {
//                System.out.println("client val's are test=" + test + ", syncFoobar=" + syncFoobar);
//            }
//        }
    }

    public float getSyncFoobar() {
        return syncFoobar;
    }
}
