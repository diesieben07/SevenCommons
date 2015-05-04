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
public enum FluidTankAndCapacitySyncer implements Syncer<FluidTank, FluidTankAndCapacitySyncer.StackAndCapacity, FluidTankAndCapacitySyncer.StackAndCapacity> {

    INSTANCE;

    @Override
    public Class<StackAndCapacity> getCompanionType() {
        return StackAndCapacity.class;
    }

    @Override
    public <T_OBJ> Change<StackAndCapacity> checkChange(T_OBJ obj, FluidTank value, StackAndCapacity companion, Consumer<StackAndCapacity> companionSetter) {
        if (companion != null && value.getCapacity() == companion.capacity && Fluids.identical(value.getFluid(), companion.stack)) {
            return noChange();
        } else {
            companion = new StackAndCapacity(value.getFluid(), value.getCapacity());
            companionSetter.accept(companion);
            return newValue(companion);
        }
    }

    @Override
    public void write(StackAndCapacity value, MCDataOutput out) {
        out.writeFluidStack(value.stack);
        out.writeVarInt(value.capacity);
    }

    @Override
    public StackAndCapacity read(MCDataInput in) {
        return new StackAndCapacity(in.readFluidStack(), in.readVarInt());
    }

    @Override
    public <T_OBJ> void applyChange(T_OBJ obj, StackAndCapacity data, FluidTank oldValue, BiConsumer<T_OBJ, FluidTank> setter) {
        oldValue.setCapacity(data.capacity);
        oldValue.setFluid(data.stack);
    }

    public static final class StackAndCapacity {

        final FluidStack stack;
        final int capacity;

        StackAndCapacity(FluidStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
        }
    }
}
