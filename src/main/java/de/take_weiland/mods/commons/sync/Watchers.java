package de.take_weiland.mods.commons.sync;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.util.Fluids;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
public final class Watchers {

	private static final Map<Class<?>, WatcherHolder> watchers = Maps.newHashMap();

	public static <T> void register(Class<T> clazz, Class<? extends PropertyWatcher<T>> watcher) {
		checkNotNull(clazz, "class");
		checkNotNull(watcher, "watcher");
		checkArgument(!watchers.containsKey(clazz), "Watcher for class %s already registered", clazz.getName());
		watchers.put(clazz, getHolder(watcher));
	}

	private static WatcherHolder getHolder(Class<?> watcher) {
		return new WatcherHolder(getConstructor(watcher), PropertyWatcher.WithSubclasses.class.isAssignableFrom(watcher));
	}

	private static <T> Constructor<T> getConstructor(Class<T> watcher) {
		try {
			Constructor<T> cstr = watcher.getDeclaredConstructor();
			cstr.setAccessible(true);
			return cstr;
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(String.format("PropertyWatcher %s has no default constructor!", watcher.getName()), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> PropertyWatcher<T> createWatcher(Class<T> clazz) {
		try {
			return getWatcherConstructor(clazz).newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY); // re-use empty array
		} catch (ReflectiveOperationException e) {
			throw Throwables.propagate(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Constructor<? extends PropertyWatcher<T>> getWatcherConstructor(Class<T> clazz) {
		WatcherHolder holder = watchers.get(clazz);
		if (holder == null) {
			holder = lookupSupers(clazz);
		}
		return (Constructor<? extends PropertyWatcher<T>>) holder.cstr;
	}

	private static WatcherHolder lookupSupers(Class<?> clazz) {
		WatcherHolder found = null;
		for (Map.Entry<Class<?>, WatcherHolder> entry : watchers.entrySet()) {
			if (entry.getKey().isAssignableFrom(clazz)) {
				if (found != null) {
					throw new IllegalStateException("Multiple candidates for watching " + clazz.getName());
				}
				found = entry.getValue();
			}
		}
		if (found == null || !found.handlesSubclasses) {
			throw new IllegalStateException(String.format("Don't know how to watch %s", clazz.getName()));
		}
		watchers.put(clazz, found);
		return found;
	}

	private static class WatcherHolder {

		final Constructor<?> cstr;
		final boolean handlesSubclasses;

		WatcherHolder(Constructor<?> cstr, boolean handlesSubclasses) {
			this.cstr = cstr;
			this.handlesSubclasses = handlesSubclasses;
		}
	}

	static {
		register(ItemStack.class, ForItemStack.class);
		register(FluidStack.class, ForFluidStack.class);
		register(String.class, ForString.class);
		register(UUID.class, ForUUID.class);
		register(FluidTank.class, ForFluidTank.class);
	}

	private static class ForItemStack extends PropertyWatcher.Standard<ItemStack> {

		private ItemStack companion;

		@Override
		public boolean hasChanged(ItemStack value) {
			return !ItemStacks.identical(value, companion);
		}

		@Override
		public void writeAndUpdate(ItemStack value, MCDataOutputStream out) {
			companion = ItemStacks.clone(value);
			out.writeItemStack(value);
		}

		@Override
		public ItemStack read(ItemStack value, MCDataInputStream in) {
			return in.readItemStack();
		}
	}

	private static class ForFluidStack extends PropertyWatcher.Standard<FluidStack> {

		private FluidStack companion;

		@Override
		public boolean hasChanged(FluidStack value) {
			return !Fluids.identical(value, companion);
		}

		@Override
		public void writeAndUpdate(FluidStack value, MCDataOutputStream out) {
			companion = Fluids.clone(value);
			out.writeFluidStack(value);
		}

		@Override
		public FluidStack read(FluidStack value, MCDataInputStream in) {
			return in.readFluidStack();
		}
	}

	private static class ForString extends PropertyWatcher.Standard<String> {

		private String companion;

		@Override
		public boolean hasChanged(String value) {
			return !Objects.equals(value, companion);
		}

		@Override
		public void writeAndUpdate(String value, MCDataOutputStream out) {
			companion = value;
			out.writeString(value);
		}

		@Override
		public String read(String value, MCDataInputStream in) {
			return in.readString();
		}
	}

	private static class ForUUID extends PropertyWatcher.Standard<UUID> {

		private UUID companion;

		@Override
		public boolean hasChanged(UUID value) {
			return !Objects.equals(value, companion);
		}

		@Override
		public void writeAndUpdate(UUID value, MCDataOutputStream out) {
			companion = value;
			out.writeUUID(value);
		}

		@Override
		public UUID read(UUID value, MCDataInputStream in) {
			return in.readUUID();
		}
	}

	private static class ForFluidTank extends PropertyWatcher.WithSubclasses<FluidTank> {

		private FluidStack companion;

		@Override
		public boolean hasChanged(FluidTank value) {
			return Fluids.identical(value.getFluid(), companion);
		}

		@Override
		public void writeAndUpdate(FluidTank value, MCDataOutputStream out) {
			FluidStack fluid = value.getFluid();
			companion = Fluids.clone(fluid);
			out.writeFluidStack(fluid);
		}

		@Override
		public void readInPlace(FluidTank value, MCDataInputStream in) {
			value.setFluid(in.readFluidStack());
		}
	}


	private Watchers() { }
}
