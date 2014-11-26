package de.take_weiland.mods.commons.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class FluidTankSyncer implements ContentSyncer<FluidTank> {

	private FluidTankSyncer() { }

	@Override
	public boolean hasChanged(FluidTank value, Object data) {
		return !Fluids.identical((FluidStack) data, value.getFluid());
	}

	@Override
	public Object writeAndUpdate(FluidTank value, MCDataOutputStream out, Object data) {
		FluidStack fluid = value.getFluid();
		out.writeFluidStack(fluid);
		return Fluids.clone(fluid);
	}

	@Override
	public void read(FluidTank value, MCDataInputStream in, Object data) {
		value.setFluid(in.readFluidStack());
	}

	private static final class WithCapacity implements ContentSyncer<FluidTank> {

		private WithCapacity() {}

		@Override
		public boolean hasChanged(FluidTank value, Object data) {
			DataObject dataObject = (DataObject) data;
			return dataObject.capComp != value.getCapacity() || !Fluids.identical(dataObject.companion, value.getFluid());
		}

		@Override
		public Object writeAndUpdate(FluidTank value, MCDataOutputStream out, Object data) {
			FluidStack fluid = value.getFluid();
			int cap = value.getCapacity();

			out.writeFluidStack(fluid);
			out.writeVarInt(cap);

			DataObject dataObject = (DataObject) data;
			dataObject.companion = Fluids.clone(fluid);
			dataObject.capComp = cap;
			return data;
		}

		@Override
		public void read(FluidTank value, MCDataInputStream in, Object data) {
			value.setFluid(in.readFluidStack());
			value.setCapacity(in.readVarInt());
		}
	}

	private static final class DataObject {

		FluidStack companion;
		int capComp;

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
//		SyncingManager.regContentSyncer(FluidTank.class, new SyncingManager.CallSiteProvider() {
//			@Override
//			public CallSite get(Class<?> caller, String member, boolean isMethod) {
//				try {
//					boolean syncCap;
//					if (isMethod) {
//						syncCap = caller.getDeclaredMethod(member).isAnnotationPresent(SyncCapacity.class);
//					} else {
//						syncCap = caller.getDeclaredField(member).isAnnotationPresent(SyncCapacity.class);
//					}
//					if (syncCap) {
//						return withCapCstr;
//					} else {
//						return noCapCstr;
//					}
//				} catch (ReflectiveOperationException e) {
//					throw new AssertionError(e);
//				}
//			}
//
//			@Override
//			public boolean handlesSubclasses() {
//				return true;
//			}
//		});
	}
}
