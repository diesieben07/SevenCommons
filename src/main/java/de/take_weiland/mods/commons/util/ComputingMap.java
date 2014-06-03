package de.take_weiland.mods.commons.util;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.sun.javafx.beans.annotations.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
public final class ComputingMap<K, V> implements Map<K, V> {

	public static <K, V> ComputingMap<K, V> create(@NonNull Function<? super K, ? extends V> function) {
		return of(new HashMap<K, V>(), function);
	}

	public static <K, V> ComputingMap<K, V> of(@NonNull Map<K, V> map, @NonNull Function<? super K, ? extends V> function) {
		return new ComputingMap<>(checkNotNull(map), checkNotNull(function));
	}

	public static <K, V> ComputingMap<K, V> of(@NonNull Map<K, V> map, @NonNull Supplier<? extends V> supplier) {
		return new ComputingMap<>(checkNotNull(map), Functions.forSupplier(supplier));
	}

	private final Map<K, V> delegate;
	private final Function<? super K, ? extends V> function;

	private ComputingMap(Map<K, V> delegate, Function<? super K, ? extends V> function) {
		this.delegate = delegate;
		this.function = function;
	}

	@Override
	public V get(Object key) {
		V value = delegate.get(key);
		if (value != null) {
			return value;
		}
		//noinspection unchecked
		delegate.put((K) key, (value = function.apply(((K) key))));
		return value;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	@Override
	public V remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public V put(K key, V value) {
		return delegate.put(key, value);
	}

	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		delegate.putAll(m);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}
}
