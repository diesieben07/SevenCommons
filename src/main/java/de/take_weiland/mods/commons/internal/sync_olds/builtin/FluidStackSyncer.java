package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
enum FluidStackSyncer implements TypeSyncer<FluidStack, FluidStack, FluidStack> {

    INSTANCE;

    @Override
    public Change<FluidStack> check(Object obj, PropertyAccess<FluidStack> property, Object cObj, PropertyAccess<FluidStack> companion) {
        FluidStack value = property.get(obj);
        if (Fluids.identical(value, companion.get(cObj))) {
            return noChange();
        } else {
            FluidStack clone = Fluids.clone(value);
            companion.set(obj, clone);
            return newValue(clone);
        }
    }

    @Override
    public Change<FluidStack> forceUpdate(Object obj, PropertyAccess<FluidStack> property, Object cObj, PropertyAccess<FluidStack> companion) {
        return newValue(Fluids.clone(property.get(obj)));
    }


    @Override
    public Class<FluidStack> companionType() {
        return FluidStack.class;
    }

    @Override
    public void encode(FluidStack stack, MCDataOutput out) {
        out.writeFluidStack(stack);
    }

    @Override
    public void apply(FluidStack fluidStack, Object obj, PropertyAccess<FluidStack> property, Object cObj, PropertyAccess<FluidStack> companion) {
        property.set(obj, Fluids.clone(fluidStack));
    }

    @Override
    public void apply(MCDataInput in, Object obj, PropertyAccess<FluidStack> property, Object cObj, PropertyAccess<FluidStack> companion) {
        property.set(obj, in.readFluidStack());
    }
}
