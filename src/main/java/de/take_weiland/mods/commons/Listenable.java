package de.take_weiland.mods.commons;

/**
 * <p>Simple Interface for implementing the <a href="http://en.wikipedia.org/wiki/Observer_pattern">Observer Pattern</a>.</p>
 * <p>Use the methods in {@link de.take_weiland.mods.commons.Listenables} to register and unregister listeners.</p>
 * <p>To notify listeners, call {@link de.take_weiland.mods.commons.Listenables#onChange(Listenable)}, which will then call
 * {@link de.take_weiland.mods.commons.Listenable.Listener#onChange(Object)} on every listener and also {@link Listenable#onChange()}
 * on the Listenable itself.</p>
 * @author diesieben07
 */
public interface Listenable<SELF extends Listenable<SELF>> {

	interface Listener<TYPE> {

		void onChange(TYPE o);

	}

	void onChange();

}