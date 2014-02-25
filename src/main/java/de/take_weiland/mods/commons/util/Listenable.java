package de.take_weiland.mods.commons.util;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.trait.Instance;
import de.take_weiland.mods.commons.trait.Trait;
import de.take_weiland.mods.commons.trait.TraitImpl;

import java.util.List;

@Trait(impl = Listenable.ListenableImpl.class)
public interface Listenable<SELF extends Listenable<SELF>> {

	public static interface Listener<E> {
		
		void onChange(E obj);
		
	}

	void registerListener(Listener<? super SELF> listener);
	void removeListener(Listener<? super SELF> listener);
	void onChange();

	static final class ListenableImpl<E extends Listenable<E>> implements TraitImpl, Listenable<E> {

		@Instance
		private E self;
		private List<Listener<? super E>> listeners;

		@Override
		public void registerListener(Listener<? super E> listener) {
			List<Listener<? super E>> listeners;
			((listeners = this.listeners) == null ? (this.listeners = Lists.newArrayList()) : listeners).add(listener);
		}

		@Override
		public void removeListener(Listener<? super E> listener) {
			List<Listener<? super E>> listeners;
			if ((listeners = this.listeners) != null) {
				listeners.remove(listener);
			}
		}

		@Override
		public void onChange() {
			List<Listener<? super E>> listeners;
			if ((listeners = this.listeners) != null) {
				int len = listeners.size();
				for (int i = 0; i < len; ++i) {
					listeners.get(i).onChange(self);
				}
			}
		}

	}

}
