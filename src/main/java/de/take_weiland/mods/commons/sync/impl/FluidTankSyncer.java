package de.take_weiland.mods.commons.sync.impl;

import com.google.common.base.Functions;
import de.take_weiland.mods.commons.internal.sync.SyncerFinder;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.SyncCapacity;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.ContextAnnotations;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * @author diesieben07
 */
public final class FluidTankSyncer implements ContentSyncer<FluidTank> {

	private FluidTankSyncer() { }

	static final SyncContext.Key<Boolean> SYNC_CAPACITY = new SyncContext.Key<>("FluidTank.Capacity");
	private static final FluidTankSyncer INSTANCE = new FluidTankSyncer();

	public static void register() {
		ContextAnnotations.register(SyncCapacity.class, SYNC_CAPACITY, Functions.constant(Boolean.TRUE));

		SyncingManager.registerSyncerFinder(new SyncerFinder() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
				if (FluidTank.class.isAssignableFrom(context.getRawType())) {
					return (ContentSyncer<T>) (context.getValue(SYNC_CAPACITY) == null ? FluidTankSyncer.INSTANCE : WithCapacity.INSTANCE);
				}
				return null;
			}

			@Override
			public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
				return null;
			}
		});
	}

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

		public static final WithCapacity INSTANCE = new WithCapacity();

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

}
