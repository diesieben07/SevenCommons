package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.tileentity.TileEntity;

import java.util.Random;

/**
 * @author diesieben07
 */
public class TestTE extends TileEntity {

    @Sync
    public String test;

    int foobarusMax;
    float syncFoobar;

    private int tick;

    @Override
    public void updateEntity() {
        if (tick++ % 10 == 0) {
            if (Sides.logical(this).isServer()) {
                test = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
                syncFoobar = new Random().nextFloat();
            } else {
                System.out.println("client val's are test=" + test + ", syncFoobar=" + syncFoobar);
            }
        }
    }
}
