package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContainerSyncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * @author diesieben07
 */
public class FluidTankSyncer implements ContainerSyncer<FluidTank> {

	private FluidStack companion;

	@Override
	public boolean hasChanged(FluidTank value) {
		return !Fluids.identical(companion, value.getFluid());
	}

	@Override
	public void writeAndUpdate(FluidTank value, MCDataOutputStream out) {
		FluidStack fluid = value.getFluid();
		companion = Fluids.clone(fluid);
		out.writeFluidStack(fluid);
	}

	@Override
	public void read(FluidTank value, MCDataInputStream in) {
		value.setFluid(in.readFluidStack());
	}
}
