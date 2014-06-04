package de.take_weiland.mods.commons.util;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>A simple implementation of a map that computes missing values with a Function.</p>
 * <p>It should be noted that this implementation violates the Map interface contract and in most cases
 * a {@link com.google.common.cache.Cache} is more appropriate.</p>
 * <p>This implementation supports null values and keys if the underlying map does,
 * but is <i>not</i> thread-safe, even if the underlying Map is.</p>
 * <p>Only the {@code get} method will compute new values.</p>
 *
 * @author diesieben07
 */
public final class ComputingMap<K, V> extends ForwardingMap<K, V> implements Map<K, V> {

	public static <K, V> ComputingMap<K, V> of(@NotNull Function<? super K, ? extends V> function) {
		return of(new HashMap<K, V>(), function);
	}

	public static <K, V> ComputingMap<K, V> of(@NotNull Map<K, V> map, @NotNull Function<? super K, ? extends V> function) {
		return new ComputingMap<>(checkNotNull(map), checkNotNull(function));
	}

	public static <K, V> ComputingMap<K, V> of(@NotNull Map<K, V> map, @NotNull Supplier<? extends V> supplier) {
		return new ComputingMap<>(checkNotNull(map), Functions.forSupplier(supplier));
	}

	private final Map<K, V> delegate;
	private final Function<? super K, ? extends V> function;

	private ComputingMap(Map<K, V> delegate, Function<? super K, ? extends V> function) {
		this.delegate = delegate;
		this.function = function;
	}

	@Override
	protected Map<K, V> delegate() {
		return delegate;
	}

	@Override
	public V get(Object key) {
		V value = delegate.get(key);
		if (value != null || delegate.containsKey(key)) {
			return value;
		}
		try {
			//noinspection unchecked
			delegate.put((K) key, (value = checkNotNull(function.apply(((K) key)), function + " was null for " + key)));
		} catch (ClassCastException e) {
			return null;
		}
		return value;
	}

}
