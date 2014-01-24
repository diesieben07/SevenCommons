package de.take_weiland.mods.commons.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SuperclassMap<C, V> implements Map<Class<? extends C>, V> {
	
	private final Map<Class<? extends C>, V> delegate;
	
	public SuperclassMap(Map<Class<? extends C>, V> delegate) {
		this.delegate = delegate;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key) {
		V result = delegate.get(key);
		if (result != null) {
			return result;
		}
		if (!(key instanceof Class)) {
			return null;
		}
		Class<?> clazz = (Class<?>) key;
		do {
			clazz = clazz.getSuperclass();
			result = delegate.get(key);
			if (result != null) {
				delegate.put((Class<? extends C>) key, result);
				return result;
			}
		} while (clazz != Object.class);
		return null;
	}
	
	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public V put(Class<? extends C> key, V value) {
		return delegate.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public void putAll(Map<? extends Class<? extends C>, ? extends V> m) {
		delegate.putAll(m);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<Class<? extends C>> keySet() {
		return delegate.keySet();
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	@Override
	public Set<Entry<Class<? extends C>, V>> entrySet() {
		return delegate.entrySet();
	}

}
