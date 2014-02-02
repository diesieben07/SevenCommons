package de.take_weiland.mods.commons.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

final class FluidStackSyncer implements TypeSyncer<FluidStack> {

	@Override
	public boolean equal(FluidStack now, FluidStack prev) {
		return Fluids.identical(now, prev);
	}

	@Override
	public void write(FluidStack instance, DataOutput out) throws IOException {
		Packets.writeFluidStack(out, instance);
	}

	@Override
	public FluidStack read(FluidStack old, DataInput in) throws IOException {
		return Packets.readFluidStack(in);
	}

}
