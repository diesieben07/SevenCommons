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
	public void setup(SyncableProperty<FluidTank> property) {

	}

	@Override
	public boolean hasChanged(SyncableProperty<FluidTank> property) {
		return !Fluids.identical(property.get().getFluid(), (FluidStack) property.getData());
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<FluidTank> property) {
		FluidStack val = property.get().getFluid();
		out.writeFluidStack(val);
		property.setData(Fluids.clone(val));
	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<FluidTank> property) {
		out.writeFluidStack(property.get().getFluid());
	}

	@Override
	public void read(MCDataInput in, SyncableProperty<FluidTank> property) {
		property.get().setFluid(in.readFluidStack());
	}

	public enum WithCapacity implements Watcher<FluidTank> {

		INSTANCE;

		@Override
		public void setup(SyncableProperty<FluidTank> property) {
			property.setData(new Data());
		}

		@Override
		public boolean hasChanged(SyncableProperty<FluidTank> property) {
			Data data = (Data) property.getData();
			FluidTank tank = property.get();
			return data.lastCap != tank.getCapacity() || !Fluids.identical(data.lastStack, tank.getFluid());
		}

		@Override
		public void writeAndUpdate(MCDataOutput out, SyncableProperty<FluidTank> property) {
			Data data = (Data) property.getData();
			FluidTank tank = property.get();

			int cap = tank.getCapacity();
			FluidStack stack = tank.getFluid();

			out.writeVarInt(cap);
			out.writeFluidStack(stack);

			data.lastCap = cap;
			data.lastStack = Fluids.clone(stack);
		}

		@Override
		public void initialWrite(MCDataOutput out, SyncableProperty<FluidTank> property) {
			FluidTank tank = property.get();
			out.writeVarInt(tank.getCapacity());
			out.writeFluidStack(tank.getFluid());
		}

		@Override
		public void read(MCDataInput in, SyncableProperty<FluidTank> property) {
			FluidTank tank = property.get();
			tank.setCapacity(in.readVarInt());
			tank.setFluid(in.readFluidStack());
		}

		private static final class Data {

			int lastCap;
			FluidStack lastStack;

		}
	}
}
