package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.*;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
class ConfiguratorImpl<T> implements SyncingConfigurator<T> {

	private static final int DONE = -1;
	private static final int NOT_SET = 0;
	private static final int VALUE = 1;
	private static final int CONTENT = 2;

	private final TypeToken<T> baseType;
	private boolean allowSubtypes;
	private Class<? extends Annotation> annotation;
	private Predicate<? super SyncElement<T>> filter;
	private int state = NOT_SET;

	ConfiguratorImpl(Class<T> baseType) {
		this(TypeToken.of(baseType));
	}

	ConfiguratorImpl(TypeToken<T> baseType) {
		this.baseType = baseType;
	}

	@Override
	public SyncingConfigurator<T> annotatedWith(Class<? extends Annotation> annotation) {
		checkState(this.annotation != null, "Annotation already set");
		this.annotation = annotation;
		return this;
	}

	@Override
	public SyncingConfigurator<T> when(Predicate<? super SyncElement<T>> filter) {
		checkState(this.filter == null, "Filter already set");
		this.filter = filter;
		return this;
	}

	@Override
	public SyncingConfigurator<? extends T> allowSubclasses() {
		allowSubtypes = true;
		return this;
	}

	@Override
	public void with(ValueSyncer<T> valueSyncer) {
		doFinish(new ConstantProvider<>(baseType, valueSyncer), false);
	}

	@Override
	public void with(Supplier<? extends ValueSyncer<T>> supplier) {
		doFinish(new SupplierProvider<>(baseType, supplier), false);
	}

	@Override
	public void with(SyncerProvider.ForValue provider) {
		doFinish(provider, false);
	}

	private void doFinish(SyncerProvider provider, boolean content) {
		List<Predicate<SyncElement<?>>> filters = Lists.newArrayListWithCapacity(3);
		if (!allowSubtypes) {
			filters.add(new PredicateExactType(baseType));
		}
		if (annotation != null) {
			filters.add(new AnnotationCheckingPredicate(annotation));
		}
		if (filter != null) {
			//cast is safe, we check all conditions before
			//noinspection unchecked
			filters.add((Predicate<SyncElement<?>>) filter);
		}
		Predicate<SyncElement<?>> completeFilter = fastAnd(filters);
		SyncingManager.registerInternal(baseType.getRawType(), new FilterProvider(completeFilter, provider), content);
	}

	private static Predicate<SyncElement<?>> fastAnd(List<Predicate<SyncElement<?>>> predicates) {
		switch (predicates.size()) {
			case 0:
				return Predicates.alwaysTrue();
			case 1:
				return predicates.get(0);
			case 2:
				return Predicates.and(predicates.get(0), predicates.get(1));
			default:
				return Predicates.and(predicates);
		}
	}

	private void checkNotDone() {
		checkState(!done, "Configurator already used");
	}

	private static final class PredicateExactType implements Predicate<SyncElement<?>> {

		private final TypeToken<?> type;

		PredicateExactType(TypeToken<?> type) {
			this.type = type;
		}

		@Override
		public boolean apply(SyncElement<?> input) {
			return input.getType().equals(type);
		}
	}

	private static final class PredicateInstanceOf implements Predicate<SyncElement<?>> {

		private final TypeToken<?> type;

		PredicateInstanceOf(TypeToken<?> type) {
			this.type = type;
		}

		@Override
		public boolean apply(SyncElement<?> input) {
			return input.getType().isAssignableFrom(type);
		}
	}

	private static final class FilterProvider implements SyncerProvider {

		private final Predicate<SyncElement<?>> filter;
		private final SyncerProvider wrapped;

		FilterProvider(Predicate<SyncElement<?>> filter, SyncerProvider wrapped) {
			this.filter = filter;
			this.wrapped = wrapped;
		}

		@Override
		public <S> Syncer<S> apply(SyncElement<S> element) {
			if (filter.apply(element)) {
				return wrapped.apply(element);
			} else {
				return null;
			}
		}
	}


	private static final class ConstantProvider<T> implements SyncerProvider {

		private final TypeToken<T> type;
		private final Syncer<T> syncer;

		ConstantProvider(TypeToken<T> type, Syncer<T> syncer) {
			this.type = type;
			this.syncer = syncer;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <S> Syncer<S> apply(SyncElement<S> element) {
			if (element.getType().equals(type)) {
				return (ValueSyncer<S>) syncer;
			} else {
				return null;
			}
		}
	}

	private static final class SupplierProvider<T> implements SyncerProvider {

		private final TypeToken<T> type;
		private final Supplier<? extends Syncer<T>> supplier;

		SupplierProvider(TypeToken<T> type, Supplier<? extends Syncer<T>> supplier) {
			this.type = type;
			this.supplier = supplier;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <S> Syncer<S> apply(SyncElement<S> element) {
			if (element.getType().equals(type)) {
				return (Syncer<S>) supplier.get();
			} else {
				return null;
			}
		}
	}

	private static final class AnnotationCheckingPredicate implements Predicate<SyncElement<?>> {

		private final Class<? extends Annotation> annotation;

		private AnnotationCheckingPredicate(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
		}

		@Override
		public boolean apply(SyncElement<?> input) {
			return input.isAnnotationPresent(annotation);
		}
	}

	private static final class PredicateCheckingFunction<T> implements Function<SyncElement<? extends T>, ValueSyncer<T>> {

		private final Function<? super SyncElement<? extends T>, ? extends ValueSyncer<T>> function;
		private final Predicate<? super SyncElement<? extends T>> predicate;

		PredicateCheckingFunction(Function<? super SyncElement<? extends T>, ? extends ValueSyncer<T>> function, Predicate<? super SyncElement<? extends T>> predicate) {
			this.function = function;
			this.predicate = predicate;
		}

		@Override
		public ValueSyncer<T> apply(SyncElement<? extends T> input) {
			if (predicate.apply(input)) {
				return function.apply(input);
			} else {
				return null;
			}
		}
	}

}
