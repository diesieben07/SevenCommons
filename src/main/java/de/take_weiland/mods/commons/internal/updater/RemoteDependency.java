package de.take_weiland.mods.commons.internal.updater;

import de.take_weiland.mods.commons.net.WritableDataBuf;

/**
 * @author diesieben07
 */
public class RemoteDependency implements Dependency {

	private final boolean isSatisfied;
	private final String display;

	public RemoteDependency(boolean isSatisfied, String display) {
		this.isSatisfied = isSatisfied;
		this.display = display;
	}

	@Override
	public boolean isSatisfied() {
		return isSatisfied;
	}

	@Override
	public String getDisplay() {
		return display;
	}

	@Override
	public void write(WritableDataBuf out) {
		throw new UnsupportedOperationException();
	}
}
