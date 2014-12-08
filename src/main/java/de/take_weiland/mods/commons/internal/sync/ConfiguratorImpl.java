package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.properties.ClassProperty;
import de.take_weiland.mods.commons.sync.*;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
class ConfiguratorImpl<T> implements SyncingConfigurator<T> {

	private final Class<T> baseType;
	private boolean allowSubtypes;
	private Class<? extends Annotation> annotation;
	private Predicate<? super ClassProperty<T>> filter;
	private boolean done;

	ConfiguratorImpl(Class<T> baseType) {
		this.baseType = baseType;
	}

	@Override
	public SyncingConfigurator<T> annotatedWith(Class<? extends Annotation> annotation) {
		checkNotDone();
		checkState(this.annotation != null, "Annotation already set");
		this.annotation = annotation;
		return this;
	}

	@Override
	public SyncingConfigurator<T> when(Predicate<? super ClassProperty<T>> filter) {
		checkNotDone();
		checkState(this.filter == null, "Filter already set");
		this.filter = filter;
		return this;
	}

	@Override
	public SyncingConfigurator<T> andSubclasses() {
		checkNotDone();
		allowSubtypes = true;
		return this;
	}

	@Override
	public void with(ValueSyncer<T> valueSyncer) {
		doFinish(new ConstantProvider<>(valueSyncer), false);
	}

	@Override
	public void with(SyncerProvider.ForValue provider) {
		doFinish(provider, false);
	}

	@Override
	public void with(SyncerProvider.ForContents provider) {
		doFinish(provider, true);
	}

	@Override
	public void with(ContentSyncer<T> contentSyncer) {
		doFinish(new ConstantProvider<>(contentSyncer), true);
	}


	private void doFinish(SyncerProvider provider, boolean content) {
		checkNotDone();
		done = true;
		List<Predicate<ClassProperty<?>>> filters = Lists.newArrayListWithCapacity(3);

		if (allowSubtypes) {
			filters.add(new PredicateInstanceOf(baseType));
		} else {
			filters.add(new PredicateExactType(baseType));
		}

		if (annotation != null) {
			filters.add(new AnnotationCheckingPredicate(annotation));
		}

		if (filter != null) {
			//cast is safe, we check all conditions before
			//noinspection unchecked
			filters.add((Predicate<ClassProperty<?>>) filter);
		}
		Predicate<ClassProperty<?>> completeFilter = fastAnd(filters);
		SyncingManager.registerInternal(baseType, new FilterProvider(completeFilter, provider), content);
	}

	private static Predicate<ClassProperty<?>> fastAnd(List<Predicate<ClassProperty<?>>> predicates) {
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

	private static final class PredicateExactType implements Predicate<ClassProperty<?>> {

		private final Class<?> type;

		PredicateExactType(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean apply(ClassProperty<?> input) {
			return input.getType().getRawType() == type;
		}
	}

	private static final class PredicateInstanceOf implements Predicate<ClassProperty<?>> {

		private final Class<?> type;

		PredicateInstanceOf(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean apply(ClassProperty<?> input) {
			return input.getType().getRawType().isAssignableFrom(type);
		}
	}

	private static final class ConstantProvider<T> implements SyncerProvider {

		private final Syncer<T> syncer;

		ConstantProvider(Syncer<T> syncer) {
			this.syncer = syncer;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <S> Syncer<S> apply(ClassProperty<S> element) {
			return (ValueSyncer<S>) syncer;
		}
	}

	private static final class FilterProvider implements SyncerProvider {
		private final Predicate<ClassProperty<?>> filter;

		private final SyncerProvider wrapped;

		FilterProvider(Predicate<ClassProperty<?>> filter, SyncerProvider wrapped) {
			this.filter = filter;
			this.wrapped = wrapped;
		}
		@Override
		public <S> Syncer<S> apply(ClassProperty<S> element) {
			if (filter.apply(element)) {
				return wrapped.apply(element);
			} else {
				return null;
			}
		}

	}

	private static final class AnnotationCheckingPredicate implements Predicate<ClassProperty<?>> {

		private final Class<? extends Annotation> annotation;

		private AnnotationCheckingPredicate(Class<? extends Annotation> annotation) {
			this.annotation = annotation;
		}

		@Override
		public boolean apply(ClassProperty<?> input) {
			return input.isAnnotationPresent(annotation);
		}
	}

}
