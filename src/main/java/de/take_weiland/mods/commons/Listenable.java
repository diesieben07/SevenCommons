package de.take_weiland.mods.commons;

/**
 * @author diesieben07
 */
public interface Listenable<SELF extends Listenable<SELF>> {

	interface Listener<TYPE> {

		void onChange(TYPE o);

	}

	void onChange();

}
