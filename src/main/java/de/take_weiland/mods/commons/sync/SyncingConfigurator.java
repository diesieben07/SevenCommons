package de.take_weiland.mods.commons.sync;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
public interface SyncingConfigurator<T> {

	SyncingConfigurator<T> annotatedWith(Class<? extends Annotation> annotation);

	SyncingConfigurator<T> when(Predicate<? super SyncElement<T>> filter);

	SyncingConfigurator<? extends T> allowSubclasses();

	void with(ValueSyncer<T> valueSyncer);

	void with(Supplier<? extends ValueSyncer<T>> supplier);

	void with(SyncerProvider.ForValue provider);

}
