package de.take_weiland.mods.commons.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import de.take_weiland.mods.commons.network.Packets;
import de.take_weiland.mods.commons.util.Fluids;

final class FluidTankSyncer implements TypeSyncer<FluidTank> {

	@Override
	public boolean equal(FluidTank now, FluidTank prev) {
		return Fluids.identical(now.getFluid(), prev.getFluid());
	}

	@Override
	public void write(FluidTank instance, DataOutput out) throws IOException {
		Packets.writeFluidStack(out, instance.getFluid());
	}

	@Override
	public FluidTank read(FluidTank old, DataInput in) throws IOException {
		FluidStack fluid = Packets.readFluidStack(in);
		if (old == null) {
			throw new IllegalStateException("Can't sync null FluidTank!");
		}
		old.setFluid(fluid);
		return old;
	}

}
