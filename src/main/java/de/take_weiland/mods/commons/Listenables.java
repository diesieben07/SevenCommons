package de.take_weiland.mods.commons;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.internal.ListenableInternal;

import java.util.List;

/**
 * @author diesieben07
 */
public final class Listenables {

	public static <T extends Listenable<T>> void register(Listenable<T> listenable, Listenable.Listener<? super T> listener) {
		List<Listenable.Listener<? super T>> list = ((ListenableInternal<T>) listenable)._sc$listeners();
		if (list == null) {
			((ListenableInternal<T>) listenable)._sc$setListeners((list = Lists.newArrayList()));
		}
		list.add(listener);
	}

	public static <T extends Listenable<T>> void unregister(Listenable<T> listenable, Listenable.Listener<? super T> listener) {
		List<Listenable.Listener<? super T>> list;
		if ((list = ((ListenableInternal<T>) listenable)._sc$listeners()) != null) {
			list.remove(listener);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Listenable<T>> void onChange(Listenable<T> listenable) {
		listenable.onChange();
		List<Listenable.Listener<? super T>> list;
		if ((list = ((ListenableInternal<T>) listenable)._sc$listeners()) != null) {
			int len = list.size();
			for (int i = 0; i < len; ++i) {
				list.get(i).onChange((T) listenable);
			}
		}
	}

	private Listenables() { }

}
