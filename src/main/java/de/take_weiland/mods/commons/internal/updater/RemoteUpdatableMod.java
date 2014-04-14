package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.URL;

/**
 * @author diesieben07
 */
public class RemoteUpdatableMod extends AbstractUpdatableMod {

	private final String modId;
	private final String name;
	private final boolean isInternal;

	public RemoteUpdatableMod(UpdateControllerRemote controller, ModVersionCollection versions, String modId, String name, boolean isInternal) {
		super(controller, versions);
		this.modId = modId;
		this.name = name;
		this.isInternal = isInternal;
	}

	@Override
	public String getModId() {
		return modId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public URL getUpdateURL() {
		return null;
	}

	@Override
	public File getSource() {
		return null;
	}

	@Override
	public boolean isInternal() {
		return isInternal;
	}
}
