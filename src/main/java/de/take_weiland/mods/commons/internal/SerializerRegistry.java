package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public abstract class SerializerRegistry<SPI> {

	private ListMultimap<Class<?>, SPI> registry = ArrayListMultimap.create();

	public SerializerRegistry() {
		SevenCommons.registerPostInitCallback(new Runnable() {
			@Override
			public void run() {
				freeze();
			}
		});
	}

	protected abstract Object applySPIContent(SPI spi, TypeSpecification<?> type);

	protected abstract Object applySPIValue(SPI spi, TypeSpecification<?> type);

	protected final Object findSerializer0(TypeSpecification<?> type) {
		// only need to synchronize if we're not frozen
		if (isFrozen()) {
			return findSerializerNoSync(type);
		} else {
			synchronized (this) {
				return findSerializerNoSync(type);
			}
		}
	}

	protected final void register0(Class<?> clazz, SPI spi) {
		synchronized (this) {
			checkNotFrozen();
			registry.put(clazz, spi);
		}
	}

	private Object findSerializerNoSync(TypeSpecification<?> type) {
		for (Class<?> clazz : JavaUtils.hierarchy(type.getRawType(), JavaUtils.Interfaces.INCLUDE)) {
			Collection<SPI> spis = registry.get(clazz);
			Object serializer = findSerializerFromSPIs(type, spis);
			if (serializer != null) {
				return serializer;
			}
		}
		throw new IllegalArgumentException("No serializer found for " + type);
	}

	private Object findSerializerFromSPIs(TypeSpecification<?> type, Collection<SPI> spis) {
		SerializationMethod serializationMethod = type.getDesiredMethod();

		Object serializer = null;
		for (SPI spi : spis) {
			Object applied;
			switch (serializationMethod) {
				case CONTENTS:
					applied = applySPIContent(spi, type);
					break;
				case VALUE:
					applied = applySPIValue(spi, type);
			}

			if (serializer == null) {
				serializer = applySPIContent(spi, type);
			} else {
				if (applySPIContent(spi, type) != null) {
					throw new IllegalStateException("Multiple Serializers for type " + type);
				}
			}
		}
		return serializer;
	}

	private void checkNotFrozen() {
		checkState(!isFrozen(), "Cannot register serializer after postInit");
	}

	private boolean isFrozen() {
		return registry instanceof ImmutableListMultimap;
	}

	final void freeze() {
		synchronized (this) {
			registry = ImmutableListMultimap.copyOf(registry);
		}
	}

}
