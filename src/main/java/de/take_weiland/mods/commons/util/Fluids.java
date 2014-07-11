package de.take_weiland.mods.commons.util;

import net.minecraftforge.fluids.FluidStack;

/**
 * <p>Utilities regarding Fluids and FluidStacks.</p>
 *
 * @see net.minecraftforge.fluids.FluidRegistry
 */
public final class Fluids {

	/**
	 * <p>Determine if the FluidStacks are identical.</p>
	 * <p>This is a null-guarded call to {@link net.minecraftforge.fluids.FluidStack#isFluidStackIdentical(net.minecraftforge.fluids.FluidStack)}.</p>
	 *
	 * @param a first FluidStack
	 * @param b second FluidStack
	 * @return true if the fluids are either both null or identical
	 */
	public static boolean identical(FluidStack a, FluidStack b) {
		return a == null ? b == null : a.isFluidStackIdentical(b);
	}

	private Fluids() {
	}

}
