package de.take_weiland.mods.commons.util;

public interface ListenerList<E> {

	void onChange();
	
	void add(Listenable.Listener<? super E> listener);
	
	void remove(Listenable.Listener<? super E> listener);
	
}
