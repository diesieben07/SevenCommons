package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * @author diesieben07
 */
enum FluidTankSyncer implements TypeSyncer<FluidTank, FluidStack, FluidStack> {

    INSTANCE;

    @Override
    public Change<FluidStack> check(Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<FluidStack> companion) {
        FluidTank tank = property.get(obj);
        if (Fluids.identical(tank.getFluid(), companion.get(cObj))) {
            return noChange();
        } else {
            FluidStack clone = Fluids.clone(tank.getFluid());
            companion.set(cObj, clone);
            return newValue(clone);
        }
    }

    @Override
    public Change<FluidStack> forceUpdate(Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<FluidStack> companion) {
        return newValue(Fluids.clone(property.get(obj).getFluid()));
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
    public void apply(FluidStack fluidStack, Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<FluidStack> companion) {
        property.get(obj).setFluid(Fluids.clone(fluidStack));
    }

    @Override
    public void apply(MCDataInput in, Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<FluidStack> companion) {
        property.get(obj).setFluid(in.readFluidStack());
    }

    enum WithCapacity implements TypeSyncer<FluidTank, StackAndCapacity, StackAndCapacity> {

        INSTANCE;

        @Override
        public Change<StackAndCapacity> check(Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<StackAndCapacity> companion) {
            FluidTank tank = property.get(obj);
            StackAndCapacity compVal = companion.get(cObj);
            if (compVal != null && tank.getCapacity() == compVal.capacity && Fluids.identical(tank.getFluid(), compVal.stack)) {
                return noChange();
            } else {
                StackAndCapacity newVal = new StackAndCapacity(Fluids.clone(tank.getFluid()), tank.getCapacity());
                companion.set(cObj, newVal);
                return newValue(newVal);
            }
        }

        @Override
        public Change<StackAndCapacity> forceUpdate(Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<StackAndCapacity> companion) {
            FluidTank tank = property.get(obj);
            return newValue(new StackAndCapacity(Fluids.clone(tank.getFluid()), tank.getCapacity()));
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
        public void apply(StackAndCapacity stackAndCapacity, Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<StackAndCapacity> companion) {
            FluidTank tank = property.get(obj);
            tank.setFluid(Fluids.clone(stackAndCapacity.stack));
            tank.setCapacity(stackAndCapacity.capacity);
        }

        @Override
        public void apply(MCDataInput in, Object obj, PropertyAccess<FluidTank> property, Object cObj, PropertyAccess<StackAndCapacity> companion) {
            FluidTank tank = property.get(obj);
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