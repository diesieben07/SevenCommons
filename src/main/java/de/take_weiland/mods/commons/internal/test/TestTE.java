package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.util.Listenable;

import java.util.Random;

/**
 * @author diesieben07
 */
public class TestTE extends SuperTE implements SyncedInterface, Listenable<String> {

    private float syncFoobar;

    @Sync
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
        if (!worldObj.isRemote) {
            syncFoobar = new Random().nextFloat();
        } else {
            if (lastRot != rotMeta) {
                worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
                lastRot = rotMeta;
            }
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

}
