package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
enum FluidTankSyncer implements Syncer.Simple<FluidTank, FluidStack, FluidStack> {

    INSTANCE;

    @Override
    public <OBJ> Change<FluidStack> check(FluidTank value, FluidStack companion, OBJ obj, BiConsumer<OBJ, FluidTank> setter, BiConsumer<OBJ, FluidStack> cSetter) {
        if (Fluids.identical(value.getFluid(), companion)) {
            return noChange();
        } else {
            FluidStack clone = Fluids.clone(value.getFluid());
            cSetter.accept(obj, clone);
            return newValue(clone);
        }
    }

    @Override
    public Class<FluidStack> companionType() {
        return FluidStack.class;
    }

    @Override
    public void encode(FluidStack fluidStack, MCDataOutput out) {
        out.writeFluidStack(fluidStack);
    }

    @Override
    public <OBJ> void apply(FluidStack fluidStack, OBJ obj, Function<OBJ, FluidTank> getter, BiConsumer<OBJ, FluidTank> setter) {
        getter.apply(obj).setFluid(Fluids.clone(fluidStack));
    }

    @Override
    public <OBJ> void apply(MCDataInput in, OBJ obj, Function<OBJ, FluidTank> getter, BiConsumer<OBJ, FluidTank> setter) {
        getter.apply(obj).setFluid(in.readFluidStack());
    }

    enum WithCapacity implements Syncer.Simple<FluidTank, StackAndCapacity, StackAndCapacity> {

        INSTANCE;

        @Override
        public <OBJ> Change<StackAndCapacity> check(FluidTank value, StackAndCapacity companion, OBJ obj, BiConsumer<OBJ, FluidTank> setter, BiConsumer<OBJ, StackAndCapacity> cSetter) {
            if (value.getCapacity() == companion.capacity && Fluids.identical(value.getFluid(), companion.stack)) {
                return noChange();
            } else {
                StackAndCapacity newVal = new StackAndCapacity(Fluids.clone(value.getFluid()), value.getCapacity());
                cSetter.accept(obj, newVal);
                return newValue(newVal);
            }
        }

        @Override
        public Class<StackAndCapacity> companionType() {
            return StackAndCapacity.class;
        }

        @Override
        public void encode(StackAndCapacity stackAndCapacity, MCDataOutput out) {
            out.writeFluidStack(stackAndCapacity.stack);
            out.writeVarInt(stackAndCapacity.capacity);
        }

        @Override
        public <OBJ> void apply(StackAndCapacity stackAndCapacity, OBJ obj, Function<OBJ, FluidTank> getter, BiConsumer<OBJ, FluidTank> setter) {
            FluidTank tank = getter.apply(obj);
            tank.setFluid(Fluids.clone(stackAndCapacity.stack));
            tank.setCapacity(stackAndCapacity.capacity);
        }

        @Override
        public <OBJ> void apply(MCDataInput in, OBJ obj, Function<OBJ, FluidTank> getter, BiConsumer<OBJ, FluidTank> setter) {
            FluidTank tank = getter.apply(obj);
            tank.setFluid(in.readFluidStack());
            tank.setCapacity(in.readVarInt());
        }
    }

    private static final class StackAndCapacity {

        final FluidStack stack;
        final int capacity;

        StackAndCapacity(FluidStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
        }

        StackAndCapacity(StackAndCapacity toCopy) {
            this.stack = Fluids.clone(toCopy.stack);
            this.capacity = toCopy.capacity;
        }

    }
}
