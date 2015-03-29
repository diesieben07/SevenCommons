package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SimpleSyncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * @author diesieben07
 */
enum FluidTankSyncer implements SimpleSyncer<FluidTank, FluidStack> {

    INSTANCE;

    @Override
    public Class<FluidTank> getValueType() {
        return FluidTank.class;
    }

    @Override
    public Class<FluidStack> getCompanionType() {
        return FluidStack.class;
    }

    @Override
    public boolean equal(FluidTank value, FluidStack companion) {
        return value != null && Fluids.identical(value.getFluid(), companion);
    }

    @Override
    public FluidStack writeAndUpdate(FluidTank value, FluidStack companion, MCDataOutput out) {
        FluidStack stack = value.getFluid();
        out.writeFluidStack(stack);
        return Fluids.clone(stack);
    }

    @Override
    public FluidTank read(FluidTank oldValue, FluidStack companion, MCDataInput in) {
        oldValue.setFluid(in.readFluidStack());
        return oldValue;
    }

}

