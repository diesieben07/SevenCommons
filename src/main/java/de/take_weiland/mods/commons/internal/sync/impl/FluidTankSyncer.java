package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.SyncCapacity;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.lang.invoke.*;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class FluidTankSyncer implements ContentSyncer<FluidTank> {

	private FluidStack companion;

	private FluidTankSyncer() { }

	@Override
	public boolean hasChanged(FluidTank value) {
		return !Fluids.identical(companion, value.getFluid());
	}

	@Override
	public void writeAndUpdate(FluidTank value, MCDataOutputStream out) {
		FluidStack fluid = value.getFluid();
		companion = Fluids.clone(fluid);
		out.writeFluidStack(fluid);
	}

	@Override
	public void read(FluidTank value, MCDataInputStream in) {
		value.setFluid(in.readFluidStack());
	}

	private static final class WithCapacity implements ContentSyncer<FluidTank> {

		private FluidStack companion;
		private int capComp;

		WithCapacity() {}

		@Override
		public boolean hasChanged(FluidTank value) {
			return capComp != value.getCapacity() || !Fluids.identical(companion, value.getFluid());
		}

		@Override
		public void writeAndUpdate(FluidTank value, MCDataOutputStream out) {
			FluidStack fluid = value.getFluid();
			int cap = value.getCapacity();
			companion = Fluids.clone(fluid);
			capComp = cap;

			out.writeFluidStack(fluid);
			out.writeVarInt(cap);
		}

		@Override
		public void read(FluidTank value, MCDataInputStream in) {
			value.setFluid(in.readFluidStack());
			value.setCapacity(in.readVarInt());
		}
	}

	private static final CallSite noCapCstr;
	private static final CallSite withCapCstr;

	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			noCapCstr = new ConstantCallSite(lookup.findConstructor(FluidTankSyncer.class, methodType(void.class))
					.asType(methodType(ContentSyncer.class)));
			withCapCstr = new ConstantCallSite(lookup.findConstructor(FluidTankSyncer.WithCapacity.class, methodType(void.class))
					.asType(methodType(ContentSyncer.class)));
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	public static void register() {
		SyncingManager.regContentSyncer(FluidTank.class, new SyncingManager.CallSiteProvider() {
			@Override
			public CallSite get(Class<?> caller, String member, boolean isMethod) {
				try {
					boolean syncCap;
					if (isMethod) {
						syncCap = caller.getDeclaredMethod(member).isAnnotationPresent(SyncCapacity.class);
					} else {
						syncCap = caller.getDeclaredField(member).isAnnotationPresent(SyncCapacity.class);
					}
					if (syncCap) {
						return withCapCstr;
					} else {
						return noCapCstr;
					}
				} catch (ReflectiveOperationException e) {
					throw new AssertionError(e);
				}
			}

			@Override
			public boolean handlesSubclasses() {
				return true;
			}
		});
	}
}
