package de.take_weiland.mods.commons.sync;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.PacketBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
class FluidStackAdapter extends SyncAdapter<FluidStack> {

	private int id;
	private int amount;
	private NBTTagCompound nbt;

	@Override
	public boolean checkAndUpdate(FluidStack newValue) {
		if (newValue == null) {
			if (id == -1) {
				return false;
			} else {
				id = -1;
				nbt = null; // gc
				return true;
			}
		} else {
			if (newValue.fluidID != id
					|| newValue.amount != amount
					|| !Objects.equal(newValue.tag, nbt)) {
				id = newValue.fluidID;
				amount = newValue.amount;
				nbt = NBT.copy(newValue.tag);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public void write(FluidStack value, PacketBuilder builder) {
		if (value == null) {
			builder.writeInt(-1);
		} else {
			builder.writeInt(value.fluidID);
			builder.writeVarInt(value.amount);
			DataBuffers.writeNbt(builder, value.tag);
		}
	}

	@Override
	public <ACTUAL_T extends FluidStack> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
		int id = buf.readInt();
		if (id == -1) {
			return null;
		} else {
			prevVal.fluidID = id;
			prevVal.amount = buf.readVarInt();
			prevVal.tag = DataBuffers.readNbt(buf);
			return prevVal;
		}
	}
}
