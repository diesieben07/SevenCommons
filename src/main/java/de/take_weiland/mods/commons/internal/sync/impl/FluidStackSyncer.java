package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
public final class FluidStackSyncer implements ValueSyncer<FluidStack> {

	@Override
	public boolean hasChanged(FluidStack value, Object data) {
		return !Fluids.identical((FluidStack) data, value);
	}

	@Override
	public Object writeAndUpdate(FluidStack value, MCDataOutputStream out, Object data) {
		out.writeFluidStack(value);
		return Fluids.clone(value);
	}

	@Override
	public FluidStack read(MCDataInputStream in, Object data) {
		return in.readFluidStack();
	}

	public static final class Contents implements ContentSyncer<FluidStack> {

		@Override
		public boolean hasChanged(FluidStack value, Object data) {
			return !Fluids.identical(value, (FluidStack) data);
		}

		@Override
		public Object writeAndUpdate(FluidStack value, MCDataOutputStream out, Object data) {
			out.writeFluidStack(value);
			return Fluids.clone(value);
		}

		@Override
		public void read(FluidStack value, MCDataInputStream in, Object data) {
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
