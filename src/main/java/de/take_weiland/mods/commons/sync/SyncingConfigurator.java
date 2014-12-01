package de.take_weiland.mods.commons.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
public interface SyncingConfigurator<T> {

	SyncingConfigurator<T> annotatedWith(Class<? extends Annotation> annotation);

	SyncingConfigurator<T> when(Predicate<? super SyncContext<? extends T>> predicate);

	void with(ValueSyncer<T> valueSyncer);

	void with(Function<? super SyncContext<? extends T>, ? extends ValueSyncer<T>> function);

	void with(Supplier<? extends ValueSyncer<T>> supplier);

}
