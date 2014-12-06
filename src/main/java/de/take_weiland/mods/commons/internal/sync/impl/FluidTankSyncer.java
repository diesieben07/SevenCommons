package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.SyncCapacity;
import de.take_weiland.mods.commons.sync.SyncElement;
import de.take_weiland.mods.commons.sync.SyncerProvider;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import static de.take_weiland.mods.commons.internal.sync.SyncingManager.sync;

/**
 * @author diesieben07
 */
public final class FluidTankSyncer implements ContentSyncer<FluidTank> {

	static final SyncContext.Key<Boolean> SYNC_CAPACITY = new SyncContext.Key<>("FluidTank.Capacity");

	private static FluidTankSyncer INSTANCE;

	static FluidTankSyncer instance() {
		if (INSTANCE == null) {
			synchronized (FluidTankSyncer.class) {
				if (INSTANCE == null) {
					INSTANCE = new FluidTankSyncer();
				}
			}
		}
		return INSTANCE;
	}

	private FluidTankSyncer() { }

	public static void register() {
		sync(FluidTank.class)
				.andSubclasses()
				.with(new SyncerProvider.ForContents() {
					@SuppressWarnings("unchecked")
					@Override
					public <S> ContentSyncer<S> apply(SyncElement<S> element) {
						if (element.isAnnotationPresent(SyncCapacity.class)) {
							return (ContentSyncer<S>) WithCapacity.instance();
						} else {
							return (ContentSyncer<S>) FluidTankSyncer.instance();
						}
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

		private static WithCapacity INSTANCE;

		static WithCapacity instance() {
			if (INSTANCE == null) {
				synchronized (WithCapacity.class) {
					if (INSTANCE == null) {
						INSTANCE = new WithCapacity();
					}
				}
			}
			return INSTANCE;
		}

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
