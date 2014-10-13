package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContainerSyncer;
import de.take_weiland.mods.commons.sync.PropertySyncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
public class FluidStackSyncer implements PropertySyncer<FluidStack> {

	private FluidStack companion;

	@Override
	public boolean hasChanged(FluidStack value) {
		return !Fluids.identical(companion, value);
	}

	@Override
	public void writeAndUpdate(FluidStack value, MCDataOutputStream out) {
		companion = Fluids.clone(value);
		out.writeFluidStack(value);
	}

	@Override
	public FluidStack read(MCDataInputStream in) {
		return in.readFluidStack();
	}

	public static class Contents implements ContainerSyncer<FluidStack> {

		private FluidStack companion;

		@Override
		public boolean hasChanged(FluidStack value) {
			return !Fluids.identical(value, companion);
		}

		@Override
		public void writeAndUpdate(FluidStack value, MCDataOutputStream out) {
			companion = Fluids.clone(value);
			out.writeFluidStack(value);
		}

		@Override
		public void read(FluidStack value, MCDataInputStream in) {
			int id = in.readVarInt();
			if (id == -1) {
				throw new IllegalArgumentException();
			}
			value.fluidID = id;
			value.amount = in.readVarInt();
			value.tag = in.readNBT();
		}
	}
}
