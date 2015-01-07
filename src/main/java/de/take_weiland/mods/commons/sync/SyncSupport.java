package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.WatcherRegistry;
import de.take_weiland.mods.commons.serialize.SerializationMethod;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Registry for registering new {@link de.take_weiland.mods.commons.sync.Watcher Watchers} or
 * {@link de.take_weiland.mods.commons.sync.WatcherSPI WatcherSPIs}.</p>
 * <p>This methods in this class can only be used <i>before PostInit</i>.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class SyncSupport {

	/**
	 * <p>Register a {@link de.take_weiland.mods.commons.sync.WatcherSPI} to be queried for properties of type
	 * {@code clazz} and superclasses.</p>
	 * @param clazz the class
	 * @param spi the SPI
	 */
	public static void registerSPI(Class<?> clazz, WatcherSPI spi) {
		WatcherRegistry.register(clazz, spi);
	}

	/**
	 * <p>Register a {@link de.take_weiland.mods.commons.sync.Watcher} for the given {@link de.take_weiland.mods.commons.serialize.SerializationMethod method}
	 * and class {@code T}. The watcher will only be used for properties which specify the exact type {@code T}.</p>
	 * @param clazz the class
	 * @param watcher the watcher
	 * @param method the method
	 */
	public static <T> void register(final Class<T> clazz, final Watcher<T> watcher, final SerializationMethod method) {
		registerSPI(clazz, new WatcherSPI() {
			@SuppressWarnings({"rawtypes", "unchecked"})
			@Override
			public Watcher provideWatcher(PropertyMetadata propertyMetadata, SerializationMethod actualMethod) {
				if (actualMethod == method && propertyMetadata.getRawType() == clazz) {
					return watcher;
				} else {
					return null;
				}
			}
		});
	}

	private SyncSupport() { }

}
