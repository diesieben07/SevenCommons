package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
enum  FluidStackSyncer implements Syncer<FluidStack, FluidStack, FluidStack> {

    INSTANCE;

    @Override
    public Class<FluidStack> getCompanionType() {
        return FluidStack.class;
    }

    @Override
    public boolean equals(FluidStack value, FluidStack companion) {
        return Fluids.identical(value, companion);
    }

    @Override
    public FluidStack getData(FluidStack value) {
        return Fluids.clone(value);
    }

    @Override
    public FluidStack clone(FluidStack value) {
        return Fluids.clone(value);
    }

    @Override
    public void write(FluidStack value, MCDataOutput out) {
        out.writeFluidStack(value);
    }

    @Override
    public FluidStack read(MCDataInput in) {
        return in.readFluidStack();
    }
}
