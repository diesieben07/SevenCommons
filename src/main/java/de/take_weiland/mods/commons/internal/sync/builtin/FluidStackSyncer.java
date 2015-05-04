package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

/**
 * @author diesieben07
 */
enum  FluidStackSyncer implements Syncer.Simple<FluidStack, FluidStack> {

    INSTANCE;

    @Override
    public Class<FluidStack> getCompanionType() {
        return FluidStack.class;
    }

    @Override
    public <T_OBJ> Change<FluidStack> checkChange(T_OBJ obj, FluidStack value, FluidStack companion, Consumer<FluidStack> companionSetter) {
        if (Fluids.identical(value, companion)) {
            return noChange();
        } else {
            FluidStack clone = Fluids.clone(value);
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
}
