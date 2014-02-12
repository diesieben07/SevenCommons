package de.take_weiland.mods.commons.util;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ListenerArrayList<E> implements ListenerList<E> {

	private final E obj;
	
	private List<Listenable.Listener<? super E>> listeners = Collections.emptyList();
	private boolean hasInit = false;
	
	private ListenerArrayList(E obj) {
		this.obj = checkNotNull(obj);
	}
	
	public static <E> ListenerArrayList<E> create(E obj) {
		return new ListenerArrayList<E>(obj);
	}
	
	@Override
	public void onChange() {
		for (Listenable.Listener<? super E> listener : listeners) {
			listener.onChange(obj);
		}
	}

	@Override
	public void add(Listenable.Listener<? super E> listener) {
		(hasInit ? listeners : (listeners = Lists.newArrayList())).add(listener);
	}

	@Override
	public void remove(Listenable.Listener<? super E> listener) {
		listeners.remove(listener);
	}

}
