package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
enum FluidStackSyncer implements Syncer.Simple<FluidStack, FluidStack, FluidStack> {

    INSTANCE;

    @Override
    public <OBJ> Change<FluidStack> check(FluidStack value, FluidStack companion, OBJ obj, BiConsumer<OBJ, FluidStack> setter, BiConsumer<OBJ, FluidStack> cSetter) {
        if (Fluids.identical(value, companion)) {
            return noChange();
        } else {
            FluidStack clone = Fluids.clone(value);
            cSetter.accept(obj, clone);
            return newValue(clone);
        }
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
    public <OBJ> void apply(FluidStack fluidStack, OBJ obj, Function<OBJ, FluidStack> getter, BiConsumer<OBJ, FluidStack> setter) {
        setter.accept(obj, Fluids.clone(fluidStack));
    }

    @Override
    public <OBJ> void apply(MCDataInput in, OBJ obj, Function<OBJ, FluidStack> getter, BiConsumer<OBJ, FluidStack> setter) {
        setter.accept(obj, in.readFluidStack());
    }
}
