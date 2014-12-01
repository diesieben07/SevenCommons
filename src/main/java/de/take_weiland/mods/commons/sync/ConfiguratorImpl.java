package de.take_weiland.mods.commons.sync;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
class ConfiguratorImpl<T> implements SyncingConfigurator<T> {

	private final Class<T> baseType;
	private List<Predicate<? super SyncContext<? extends T>>> filters = Lists.newArrayList();
	private boolean done;

	ConfiguratorImpl(Class<T> baseType) {
		this.baseType = baseType;
	}

	@Override
	public SyncingConfigurator<T> annotatedWith(Class<? extends Annotation> annotation) {
		return when(new AnnotationCheckingPredicate<T>(annotation));
	}

	@Override
	public SyncingConfigurator<T> when(Predicate<? super SyncContext<? extends T>> predicate) {
		checkNotDone();
		filters.add(predicate);
		return this;
	}

	@Override
	public void with(ValueSyncer<T> valueSyncer) {
		with(Functions.constant(valueSyncer));
	}

	@Override
	public void with(Supplier<? extends ValueSyncer<T>> supplier) {
		with(Functions.forSupplier(supplier));
	}

	@Override
	public void with(Function<? super SyncContext<? extends T>, ? extends ValueSyncer<T>> function) {
		checkNotDone();
		done = true;
		if (!filters.isEmpty()) {
			Predicate<? super SyncContext<? extends T>> filter;
			if (filters.size() == 1) {
				filter = filters.get(0);
			} else {
				filter = Predicates.and(filters);
			}
			function = JavaUtils.doIfElse(filter, function, Functions.constant((ValueSyncer<T>) null));
		}

	}

	private void checkNotDone() {
		checkState(!done, "Configurator already used");
	}

	private static final class AnnotationCheckingPredicate<T> implements Predicate<SyncContext<? extends T>> {

		private final Class<? extends Annotation> annotation;

		private AnnotationCheckingPredicate(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
		}

		@Override
		public boolean apply(SyncContext<? extends T> input) {
			return input.isAnnotationPresent(annotation);
		}
	}

	private static final class PredicateCheckingFunction<T> implements Function<SyncContext<? extends T>, ValueSyncer<T>> {

		private final Function<? super SyncContext<? extends T>, ? extends ValueSyncer<T>> function;
		private final Predicate<? super SyncContext<? extends T>> predicate;

		PredicateCheckingFunction(Function<? super SyncContext<? extends T>, ? extends ValueSyncer<T>> function, Predicate<? super SyncContext<? extends T>> predicate) {
			this.function = function;
			this.predicate = predicate;
		}

		@Override
		public ValueSyncer<T> apply(SyncContext<? extends T> input) {
			if (predicate.apply(input)) {
				return function.apply(input);
			} else {
				return null;
			}
		}
	}

}
