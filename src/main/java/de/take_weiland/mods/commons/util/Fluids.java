package de.take_weiland.mods.commons.util;

import net.minecraftforge.fluids.FluidStack;

public final class Fluids {

	private Fluids() { }
	
	public static boolean identical(FluidStack a, FluidStack b) {
		return a == null ? b == null : a.isFluidStackIdentical(b);
	}

}
