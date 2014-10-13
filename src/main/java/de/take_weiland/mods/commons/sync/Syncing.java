package de.take_weiland.mods.commons.sync;

import com.google.common.base.Supplier;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class Syncing {

	public static <T> void registerSyncer(@Nonnull Class<T> clazz, @Nonnull Class<? extends PropertySyncer<T>> syncer) {
		SyncingManager.regSyncer(clazz, syncer);
	}

	public static <T> void registerContainerSyncer(@Nonnull Class<T> clazz, @Nonnull Class<? extends ContainerSyncer<T>> watcher) {
		SyncingManager.regContainerSyncer(clazz, watcher);
	}

	public static <T> void registerCustomSyncer(@Nonnull Class<T> clazz, @Nonnull Supplier<? extends PropertySyncer<T>> supplier) {
		SyncingManager.regCustomSyncer(clazz, supplierGet().bindTo(supplier), false);
	}

	public static <T> void registerCustomContainerSyncer(@Nonnull Class<T> clazz, @Nonnull Supplier<? extends ContainerSyncer<T>> supplier) {
		SyncingManager.regCustomWatcher(clazz, supplierGet().bindTo(supplier), false);
	}

	private static MethodHandle supplierGet;

	private static MethodHandle supplierGet() {
		if (supplierGet == null) {
			try {
				supplierGet = MethodHandles.publicLookup().findVirtual(Supplier.class, "get", methodType(Object.class));
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}
		return supplierGet;
	}

	public static <T extends IFluidTank> void registerFluidTankImpl(@Nonnull Class<T> clazz, @Nonnull FluidTankSetter<T> setter) {
		if (customTankCstr == null) {
			try {
				customTankCstr = MethodHandles.lookup()
						.findConstructor(CustomFluidTankSyncer.class, methodType(FluidTankSetter.class));
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
		SyncingManager.regCustomWatcher(clazz, customTankCstr.bindTo(setter), true);
	}

	private static MethodHandle customTankCstr;

	private static final class CustomFluidTankSyncer<T extends IFluidTank> implements ContainerSyncer<T> {

		private final FluidTankSetter<T> setter;
		private FluidStack companion;

		private CustomFluidTankSyncer(FluidTankSetter<T> setter) {
			this.setter = setter;
		}

		@Override
		public boolean hasChanged(T value) {
			return !Fluids.identical(value.getFluid(), companion);
		}

		@Override
		public void writeAndUpdate(T value, MCDataOutputStream out) {
			FluidStack fluid = value.getFluid();
			companion = Fluids.clone(fluid);
			out.writeFluidStack(fluid);
		}

		@Override
		public void read(T value, MCDataInputStream in) {
			setter.setFluid(value, in.readFluidStack());
		}
	}

	public interface FluidTankSetter<T extends IFluidTank> {

		void setFluid(T tank, FluidStack stack);

	}

	private Syncing() { }
}
