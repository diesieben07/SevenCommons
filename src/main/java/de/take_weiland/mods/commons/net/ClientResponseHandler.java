package de.take_weiland.mods.commons.net;

/**
 * @author diesieben07
 */
public interface ClientResponseHandler<T> {

	void onResponse(T response);

}
