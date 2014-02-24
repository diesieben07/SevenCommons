package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.internal.ListenableInternal;

/**
 * @author diesieben07
 */
public final class Listenables {

	private Listenables() { }

	public static <T extends Listenable<T>> void add(Listenable<T> listenable, Listenable.Listener<? super T> listener) {
		((ListenableInternal<T>) listenable)._sc_registerListener(listener);
	}

	public static <T extends Listenable<T>> void remove(Listenable<T> listenable, Listenable.Listener<? super T> listener) {
		((ListenableInternal<T>) listenable)._sc_removeListener(listener);
	}

}
