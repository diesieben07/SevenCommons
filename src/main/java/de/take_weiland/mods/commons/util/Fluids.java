package de.take_weiland.mods.commons.util;

import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

/**
 * <p>Utilities regarding Fluids and FluidStacks.</p>
 *
 * @see net.minecraftforge.fluids.FluidRegistry
 */
public final class Fluids {

    /**
     * <p>Determine if the FluidStacks are identical.</p>
     * <p>This is a null-guarded call to {@link FluidStack#isFluidStackIdentical(FluidStack)}.</p>
     *
     * @param a first FluidStack
     * @param b second FluidStack
     * @return true if the fluids are either both null or identical
     */
    public static boolean identical(@Nullable FluidStack a, @Nullable FluidStack b) {
        return a == b || (a != null && a.isFluidStackIdentical(b));
    }

    /**
     * <p>Clone the given FluidStack.</p>
     * <p>This is a null-guarded call to {@link FluidStack#clone()}.</p>
     *
     * @param stack the FluidStack
     * @return a clone
     */
    @Contract("null -> null")
    public static FluidStack clone(@Nullable FluidStack stack) {
        return stack == null ? null : stack.copy();
    }

    private Fluids() {
    }

}
