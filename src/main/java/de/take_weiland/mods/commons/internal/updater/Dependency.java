package de.take_weiland.mods.commons.internal.updater;

import de.take_weiland.mods.commons.net.WritableDataBuf;

/**
 * @author diesieben07
 */
public interface Dependency {

	boolean isSatisfied();


	String getDisplay();

	void write(WritableDataBuf out);
}
