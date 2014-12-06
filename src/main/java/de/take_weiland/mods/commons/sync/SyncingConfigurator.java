package de.take_weiland.mods.commons.sync;

import com.google.common.base.Predicate;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
public interface SyncingConfigurator<T> {

	SyncingConfigurator<T> annotatedWith(Class<? extends Annotation> annotation);

	SyncingConfigurator<T> when(Predicate<? super SyncElement<T>> filter);

	SyncingConfigurator<T> andSubclasses();

	void with(ValueSyncer<T> valueSyncer);

	void with(SyncerProvider.ForValue provider);

	void with(ContentSyncer<T> contentSyncer);

	void with(SyncerProvider.ForContents provider);

}
