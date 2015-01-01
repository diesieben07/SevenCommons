package de.take_weiland.mods.commons.internal.syncimpl;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * @author diesieben07
 */
public enum FluidTankWatcher implements Watcher<FluidTank> {

	INSTANCE;

	@Override
	public <OBJ> void setup(SyncableProperty<FluidTank, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
		return !Fluids.identical(property.get(instance).getFluid(), (FluidStack) property.getData(instance));
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
		FluidStack val = property.get(instance).getFluid();
		out.writeFluidStack(val);
		property.setData(Fluids.clone(val), instance);
	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
		out.writeFluidStack(property.get(instance).getFluid());
	}

	@Override
	public <OBJ> void read(MCDataInput in, SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
		property.get(instance).setFluid(in.readFluidStack());
	}

	public enum WithCapacity implements Watcher<FluidTank> {

		INSTANCE;

		@Override
		public <OBJ> void setup(SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
			property.setData(new Data(), instance);
		}

		@Override
		public <OBJ> boolean hasChanged(SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
			Data data = (Data) property.getData(instance);
			FluidTank tank = property.get(instance);
			return data.lastCap != tank.getCapacity() || !Fluids.identical(data.lastStack, tank.getFluid());
		}

		@Override
		public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
			Data data = (Data) property.getData(instance);
			FluidTank tank = property.get(instance);

			int cap = tank.getCapacity();
			FluidStack stack = tank.getFluid();

			out.writeVarInt(cap);
			out.writeFluidStack(stack);

			data.lastCap = cap;
			data.lastStack = Fluids.clone(stack);
		}

		@Override
		public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
			FluidTank tank = property.get(instance);
			out.writeVarInt(tank.getCapacity());
			out.writeFluidStack(tank.getFluid());
		}

		@Override
		public <OBJ> void read(MCDataInput in, SyncableProperty<FluidTank, OBJ> property, OBJ instance) {
			FluidTank tank = property.get(instance);
			tank.setCapacity(in.readVarInt());
			tank.setFluid(in.readFluidStack());
		}

		private static final class Data {

			int lastCap;
			FluidStack lastStack;

		}
	}
}
