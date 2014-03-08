package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

final class FluidStackSyncer implements TypeSyncer<FluidStack> {

	@Override
	public boolean equal(FluidStack now, FluidStack prev) {
		return Fluids.identical(now, prev);
	}

	@Override
	public void write(FluidStack instance, WritableDataBuf out) {
		DataBuffers.writeFluidStack(out, instance);
	}

	@Override
	public FluidStack read(FluidStack old, DataBuf in) {
		return DataBuffers.readFluidStack(in);
	}

}
