package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.util.Listenable;

/**
 * @author diesieben07
 */
public interface ListenableInternal<SELF extends Listenable<SELF>> extends Listenable<SELF> {

	void _sc_registerListener(Listener<? super SELF> listener);

	void _sc_removeListener(Listener<? super SELF> listener);

}
