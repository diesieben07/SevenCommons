package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * @author diesieben07
 */
public enum FluidTankAndCapacitySyncer implements Syncer<FluidTank, FluidTankAndCapacitySyncer.Companion> {

    INSTANCE;

    @Override
    public Class<Companion> getCompanionType() {
        return Companion.class;
    }

    @Override
    public boolean equal(FluidTank value, Companion companion) {
        return companion != null
                && value != null
                && value.getCapacity() == companion.capacity
                && Fluids.identical(value.getFluid(), companion.stack);
    }

    @Override
    public Companion writeAndUpdate(FluidTank value, Companion companion, MCDataOutput out) {
        if (companion == null) {
            companion = new Companion();
        }
        int cap = value.getCapacity();
        FluidStack stack = value.getFluid();

        out.writeVarInt(cap);
        out.writeFluidStack(stack);

        companion.capacity = cap;
        companion.stack = Fluids.clone(stack);
        return companion;
    }

    @Override
    public FluidTank read(FluidTank value, Companion companion, MCDataInput in) {
        int cap = in.readVarInt();
        FluidStack stack = in.readFluidStack();

        if (value != null) {
            value.setCapacity(cap);
            value.setFluid(stack);
        }
        return value;
    }

    public static final class Companion {

        FluidStack stack;
        int capacity;

    }
}
