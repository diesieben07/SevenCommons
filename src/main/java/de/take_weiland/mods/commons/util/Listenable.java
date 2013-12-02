package de.take_weiland.mods.commons.util;

public interface Listenable<E extends Listenable<E>> {

	public static interface Listener<E> {
		
		void onChange(E obj);
		
	}
	
	void addListener(Listener<? super E> l);
	
	void removeListener(Listener<? super E> l);

}
