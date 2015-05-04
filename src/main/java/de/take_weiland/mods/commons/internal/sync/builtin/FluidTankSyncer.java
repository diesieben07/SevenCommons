package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author diesieben07
 */
enum FluidTankSyncer implements Syncer<FluidTank, FluidStack, FluidStack> {

    INSTANCE;

    @Override
    public Class<FluidStack> getCompanionType() {
        return FluidStack.class;
    }

    @Override
    public <T_OBJ> Change<FluidStack> checkChange(T_OBJ obj, FluidTank value, FluidStack companion, Consumer<FluidStack> companionSetter) {
        if (Fluids.identical(value.getFluid(), companion)) {
            return noChange();
        } else {
            FluidStack clone = Fluids.clone(value.getFluid());
            companionSetter.accept(clone);
            return newValue(clone);
        }
    }

    @Override
    public void write(FluidStack value, MCDataOutput out) {
        out.writeFluidStack(value);
    }

    @Override
    public FluidStack read(MCDataInput in) {
        return in.readFluidStack();
    }

    @Override
    public <T_OBJ> void applyChange(T_OBJ obj, FluidStack fluidStack, FluidTank oldValue, BiConsumer<T_OBJ, FluidTank> setter) {
        oldValue.setFluid(fluidStack);
    }

}

