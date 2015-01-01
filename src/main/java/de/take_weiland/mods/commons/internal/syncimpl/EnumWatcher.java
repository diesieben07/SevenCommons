package de.take_weiland.mods.commons.internal.syncimpl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
public final class EnumWatcher<E extends Enum<E>> implements Watcher<E> {

	private static final LoadingCache<Class<?>, EnumWatcher<?>> cache = CacheBuilder.newBuilder()
			.concurrencyLevel(2)
			.build(new CacheLoader<Class<?>, EnumWatcher<?>>() {
				@SuppressWarnings({"unchecked", "rawtypes"})
				@Override
				public EnumWatcher<?> load(@Nonnull Class<?> key) {
					return new EnumWatcher((Class) key);
				}
			});

	public static Watcher<?> get(Class<?> clazz) {
		return cache.getUnchecked(clazz);
	}

	private final Class<E> enumClass;

	EnumWatcher(Class<E> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public <OBJ> void setup(SyncableProperty<E, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<E, OBJ> property, OBJ instance) {
		return property.get(instance) != property.getData(instance);
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<E, OBJ> property, OBJ instance) {
		E val = property.get(instance);
		out.writeEnum(val);
		property.setData(val, instance);
	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<E, OBJ> property, OBJ instance) {
		out.writeEnum(property.get(instance));
	}

	@Override
	public <OBJ> void read(MCDataInput in, SyncableProperty<E, OBJ> property, OBJ instance) {
		property.set(in.readEnum(enumClass), instance);
	}
}
