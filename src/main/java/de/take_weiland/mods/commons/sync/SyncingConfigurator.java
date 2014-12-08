package de.take_weiland.mods.commons.sync;

import com.google.common.base.Predicate;
import de.take_weiland.mods.commons.properties.ClassProperty;

import java.lang.annotation.Annotation;

/**
 * <p>Implementation of the fluid interface for {@link de.take_weiland.mods.commons.sync.Syncing#sync(Class)}.</p>
 * @author diesieben07
 */
public interface SyncingConfigurator<T> {

	/**
	 * <p>Add a filter to only sync properties annotated with the given annotation.</p>
	 * @param annotation the annotation class
	 * @return this
	 */
	SyncingConfigurator<T> annotatedWith(Class<? extends Annotation> annotation);

	/**
	 * <p>Add a filter for properties.</p>
	 * @param filter the filter
	 * @return this
	 */
	SyncingConfigurator<T> when(Predicate<? super ClassProperty<T>> filter);

	/**
	 * <p>Allow subclasses of {@code T} as well.</p>
	 * @return this
	 */
	SyncingConfigurator<T> andSubclasses();

	/**
	 * <p>Sync this configuration with the given ValueSyncer.</p>
	 * @param valueSyncer the ValueSyncer
	 */
	void with(ValueSyncer<T> valueSyncer);

	/**
	 * <p>Sync this configuration with the given provider.</p>
	 * @param provider the provider
	 */
	void with(SyncerProvider.ForValue provider);

	/**
	 * <p>Sync this configuration with the given ContentSyncer</p>
	 * @param contentSyncer the ContentSyncer
	 */
	void with(ContentSyncer<T> contentSyncer);

	/**
	 * <p>Sync this configuration with the given provider.</p>
	 * @param provider the provider
	 */
	void with(SyncerProvider.ForContents provider);

}
