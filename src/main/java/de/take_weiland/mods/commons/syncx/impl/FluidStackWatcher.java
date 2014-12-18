package de.take_weiland.mods.commons.syncx.impl;

import de.take_weiland.mods.commons.syncx.SyncableProperty;
import de.take_weiland.mods.commons.syncx.Watcher;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
public enum FluidStackWatcher implements Watcher<FluidStack> {
    INSTANCE;

    @Override
    public void setup(SyncableProperty<? extends FluidStack> property) { }

    @Override
    public boolean hasChanged(SyncableProperty<? extends FluidStack> property) {
        return !Fluids.identical(property.get(), (FluidStack) property.getData());
    }

    @Override
    public void afterWrite(SyncableProperty<? extends FluidStack> property) {
        property.setData(Fluids.clone(property.get()));
    }
}
