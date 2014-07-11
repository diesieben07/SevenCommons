package de.take_weiland.mods.commons.sync;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.nbt.NBT;
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
}
