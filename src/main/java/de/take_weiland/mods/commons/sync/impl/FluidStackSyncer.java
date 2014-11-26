package de.take_weiland.mods.commons.sync.impl;

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

	public FluidStackSyncer() { }

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

		public Contents() { }

		@Override
		public boolean hasChanged(FluidStack value, Object data) {
			return !Fluids.identical(value, (FluidStack) data);
		}

		@Override
		public Object writeAndUpdate(FluidStack value, MCDataOutputStream out, Object data) {
			out.writeInt(value.fluidID);
			out.writeVarInt(value.amount);
			out.writeNBT(value.tag);
			return Fluids.clone(value);
		}

		@Override
		public void read(FluidStack value, MCDataInputStream in, Object data) {
			value.fluidID = in.readInt();
			value.amount = in.readVarInt();
			value.tag = in.readNBT();
		}
	}
}
