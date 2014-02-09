package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

final class FluidTankSyncer implements TypeSyncer<FluidTank> {

	@Override
	public boolean equal(FluidTank now, FluidTank prev) {
		return Fluids.identical(now.getFluid(), prev.getFluid());
	}

	@Override
	public void write(FluidTank instance, WritableDataBuf out) {
		Packets.writeFluidStack(out, instance.getFluid());
	}

	@Override
	public FluidTank read(FluidTank old, DataBuf in) {
		FluidStack fluid = Packets.readFluidStack(in);
		if (old == null) {
			throw new IllegalArgumentException("Can't sync null FluidTank!");
		}
		old.setFluid(fluid);
		return old;
	}

}
