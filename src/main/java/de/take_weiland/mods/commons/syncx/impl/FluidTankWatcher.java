package de.take_weiland.mods.commons.syncx.impl;

import de.take_weiland.mods.commons.syncx.SyncableProperty;
import de.take_weiland.mods.commons.syncx.Watcher;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * @author diesieben07
 */
public enum  FluidTankWatcher implements Watcher<FluidTank> {

    INSTANCE;

    @Override
    public void setup(SyncableProperty<? extends FluidTank> property) { }

    @Override
    public boolean hasChanged(SyncableProperty<? extends FluidTank> property) {
        return !Fluids.identical(property.get().getFluid(), (FluidStack) property.getData());
    }

    @Override
    public void afterWrite(SyncableProperty<? extends FluidTank> property) {
        property.setData(Fluids.clone(property.get().getFluid()));
    }
}
