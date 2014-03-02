package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.Listenable;

import java.util.List;

/**
 * @author diesieben07
 */
public interface ListenableInternal<SELF extends Listenable<SELF>> extends Listenable<SELF> {

	List<Listener<? super SELF>> _sc$listeners();

	void _sc$setListeners(List<Listener<? super SELF>> list);

}
