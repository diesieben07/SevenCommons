package de.take_weiland.mods.commons.internal.syncimpl;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
public enum FluidStackWatcher implements Watcher<FluidStack> {
	VALUE {
		@Override
		public void read(MCDataInput in, SyncableProperty<FluidStack> property) {
			property.set(in.readFluidStack());
		}
	},
	CONTENTS {
		@Override
		public void read(MCDataInput in, SyncableProperty<FluidStack> property) {
			FluidStack val = property.get();
			val.fluidID = in.readVarInt();
			val.amount = in.readVarInt();
			val.tag = in.readNBT();
		}
	};

	@Override
	public void setup(SyncableProperty<FluidStack> property) {

	}

	@Override
	public boolean hasChanged(SyncableProperty<FluidStack> property) {
		return !Fluids.identical(property.get(), (FluidStack) property.getData());
	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<FluidStack> property) {
		out.writeFluidStack(property.get());
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<FluidStack> property) {
		FluidStack val = property.get();
		out.writeFluidStack(val);
		property.setData(Fluids.clone(val));
	}


}
