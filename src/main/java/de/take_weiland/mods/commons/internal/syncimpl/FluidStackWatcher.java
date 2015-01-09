package de.take_weiland.mods.commons.internal.syncimpl;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
public enum FluidStackWatcher implements Watcher<FluidStack> {

	@Watcher.Provider(forType = FluidStack.class, method = SerializationMethod.VALUE)
	VALUE {
		@Override
		public <OBJ> void read(MCDataInput in, SyncableProperty<FluidStack, OBJ> property, OBJ instance) {
			property.set(in.readFluidStack(), instance);
		}
	},

	@Watcher.Provider(forType = FluidStack.class, method = SerializationMethod.CONTENTS)
	CONTENTS {
		@Override
		public <OBJ> void read(MCDataInput in, SyncableProperty<FluidStack, OBJ> property, OBJ instance) {
			FluidStack val = property.get(instance);
			val.fluidID = in.readVarInt();
			val.amount = in.readVarInt();
			val.tag = in.readNBT();
		}
	};

	@Override
	public <OBJ> void setup(SyncableProperty<FluidStack, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<FluidStack, OBJ> property, OBJ instance) {
		return !Fluids.identical(property.get(instance), (FluidStack) property.getData(instance));
	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<FluidStack, OBJ> property, OBJ instance) {
		out.writeFluidStack(property.get(instance));
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<FluidStack, OBJ> property, OBJ instance) {
		FluidStack val = property.get(instance);
		out.writeFluidStack(val);
		property.setData(Fluids.clone(val), instance);
	}


}
