package de.take_weiland.mods.commons.internal.syncimpl;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public abstract class EnumSetWatcher<E extends Enum<E>> implements Watcher<EnumSet<E>> {

	private static LoadingCache<Class<?>, EnumSetWatcher.Contents<?>> contentsCache;
	private static LoadingCache<Class<?>, EnumSetWatcher.Value<?>> valueCache;

	static {
		contentsCache = CacheBuilder.newBuilder().concurrencyLevel(2).build(new CacheLoader<Class<?>, Contents<?>>() {
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public Contents<?> load(@Nonnull Class<?> key) throws Exception {
				return new EnumSetWatcher.Contents((Class) key);
			}
		});

		valueCache = CacheBuilder.newBuilder().concurrencyLevel(2).build(new CacheLoader<Class<?>, Value<?>>() {
			@SuppressWarnings({"unchecked", "rawtypes"})
			@Override
			public Value<?> load(@Nonnull Class<?> key) throws Exception {
				return new EnumSetWatcher.Value((Class) key);
			}
		});
	}

	public static Watcher<?> getValueWatcher(Class<?> enumClass) {
		return valueCache.getUnchecked(enumClass);
	}

	public static Watcher<?> getContentsWatcher(Class<?> enumClass) {
		return contentsCache.getUnchecked(enumClass);
	}

	final Class<E> enumType;

	EnumSetWatcher(Class<E> enumType) {
		this.enumType = enumType;
	}

	@Override
	public void setup(SyncableProperty<EnumSet<E>> property) {

	}

	@Override
	public boolean hasChanged(SyncableProperty<EnumSet<E>> property) {
		return !Objects.equal(property.get(), property.getData());
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<EnumSet<E>> property) {
		EnumSet<E> val = property.get();
		out.writeEnumSet(val);
		property.setData(val.clone());
	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<EnumSet<E>> property) {
		out.writeEnumSet(property.get());
	}

	static final class Value<E extends Enum<E>> extends EnumSetWatcher<E> {

		Value(Class<E> enumType) {
			super(enumType);
		}

		@Override
		public void read(MCDataInput in, SyncableProperty<EnumSet<E>> property) {
			property.set(in.readEnumSet(enumType));
		}
	}

	static final class Contents<E extends Enum<E>> extends EnumSetWatcher<E> {

		Contents(Class<E> enumType) {
			super(enumType);
		}

		@Override
		public void read(MCDataInput in, SyncableProperty<EnumSet<E>> property) {
			in.readEnumSet(enumType, property.get());
		}
	}
}
