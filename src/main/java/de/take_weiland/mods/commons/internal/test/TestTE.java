package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.tileentity.TileEntity;

/**
 * @author diesieben07
 */
public class TestTE extends TileEntity {

    @Sync
    public String test = "__nullval__";

    @Sync
    Integer foobarusMax;

    @Sync @ToNbt
    float syncFoobar;

    private int tick;

    @ToNbt
    private String getTest() {
        return test;
    }

    private void setTest(String test) {
        this.test = test;
    }

    @Override
    public void updateEntity() {
//        if (tick++ % 10 == 0) {
//            if (Sides.logical(this).isServer()) {
//                test = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
//                syncFoobar = new Random().nextFloat();
//            } else {
//                System.out.println("client val's are test=" + test + ", syncFoobar=" + syncFoobar);
//            }
//        }
    }
}
