package de.take_weiland.mods.commons.sync.impl;

import com.google.common.base.Objects;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.SyncElement;
import de.take_weiland.mods.commons.sync.SyncerProvider;
import de.take_weiland.mods.commons.sync.ValueSyncer;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

import static de.take_weiland.mods.commons.internal.sync.SyncingManager.sync;

/**
 * @author diesieben07
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class EnumSetSyncer<E extends Enum<E>> implements ValueSyncer<EnumSet<E>> {

	private static final ConcurrentMap<Class<?>, EnumSetSyncer<?>> valueCache;
	private static final ConcurrentMap<Class<?>, EnumSetSyncer.Contents<?>> contentCache;

	static {
		MapMaker mm = new MapMaker().concurrencyLevel(2);
		valueCache = mm.makeMap();
		contentCache = mm.makeMap();
	}

	public static void register() {
		sync(EnumSet.class)
				.with(new SyncerProvider.ForValue() {
					@Override
					public <S> ValueSyncer<S> apply(SyncElement<S> element) {
						Class setValues = getEnumSetType(element.getType());
						if (setValues == null) {
							return null;
						}
						EnumSetSyncer syncer = valueCache.get(setValues);
						if (syncer == null) {
							syncer = new EnumSetSyncer(setValues);
							if (valueCache.putIfAbsent(setValues, syncer) != null) {
								syncer = valueCache.get(setValues);
							}
						}
						return syncer;
					}
				});

		sync(EnumSet.class)
				.with(new SyncerProvider.ForContents() {
					@Override
					public <S> ContentSyncer<S> apply(SyncElement<S> element) {
						Class setValues = getEnumSetType(element.getType());
						if (setValues == null) {
							return null;
						}

						Contents syncer = contentCache.get(setValues);
						if (syncer == null) {
							syncer = new EnumSetSyncer.Contents(setValues);
							if (contentCache.putIfAbsent(setValues, syncer) != null) {
								syncer = contentCache.get(setValues);
							}
						}
						return syncer;
					}
				});
	}

	private static final Type iterableType = Iterable.class.getTypeParameters()[0];

	static Class getEnumSetType(TypeToken enumSetType) {
		Class clazz = enumSetType.resolveType(iterableType).getRawType();
		if (!clazz.isEnum()) {
			return null;
		}
		return clazz;
	}

	private final Class<E> clazz;

	EnumSetSyncer(Class<E> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean hasChanged(EnumSet<E> value, Object data) {
		return !Objects.equal(value, data);
	}

	@Override
	public Object writeAndUpdate(EnumSet<E> value, MCDataOutputStream out, Object data) {
		out.writeEnumSet(value);

		@SuppressWarnings("unchecked")
		EnumSet<E> companion = (EnumSet<E>) data;

		if (value == null) {
			companion = null;
		} else if (companion == null) {
			companion = value.clone();
		} else {
			companion.clear();
			companion.addAll(value);
		}
		return companion;
	}

	@Override
	public EnumSet<E> read(MCDataInputStream in, Object data) {
		return in.readEnumSet(clazz);
	}

	public static final class Contents<E extends Enum<E>> implements ContentSyncer<EnumSet<E>> {

		private final Class<E> clazz;

		public Contents(Class<E> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean hasChanged(EnumSet<E> value, Object data) {
			return !Objects.equal(value, data);
		}

		@Override
		public Object writeAndUpdate(EnumSet<E> value, MCDataOutputStream out, Object data) {
			out.writeEnumSet(value);

			@SuppressWarnings("unchecked")
			EnumSet<E> companion = (EnumSet<E>) data;

			if (companion == null) {
				companion = value.clone();
			} else {
				companion.clear();
				companion.addAll(value);
			}
			return companion;
		}

		@Override
		public void read(EnumSet<E> value, MCDataInputStream in, Object data) {
			in.readEnumSet(clazz, value);
		}
	}

}
