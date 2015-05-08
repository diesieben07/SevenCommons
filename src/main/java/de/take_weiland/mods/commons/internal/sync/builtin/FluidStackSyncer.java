package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.AbstractSyncer;
import de.take_weiland.mods.commons.sync.PropertyAccess;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
final class FluidStackSyncer extends AbstractSyncer.WithCompanion<FluidStack, FluidStack, FluidStack> {

    protected <OBJ> FluidStackSyncer(OBJ obj, PropertyAccess<OBJ, FluidStack> property) {
        super(obj, property);
    }

    @Override
    protected Change<FluidStack> check(FluidStack value) {
        if (Fluids.identical(value, companion)) {
            return noChange();
        } else {
            FluidStack clone = Fluids.clone(value);
            companion = clone;
            return newValue(clone);
        }
    }

    @Override
    public void encode(FluidStack stack, MCDataOutput out) {
        out.writeFluidStack(stack);
    }

    @Override
    public void apply(FluidStack stack) {
        set(stack);
    }

    @Override
    public void apply(MCDataInput in) {
        set(in.readFluidStack());
    }
}
