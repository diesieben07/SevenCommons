package de.take_weiland.mods.commons.internal.updater;

/**
 * @author diesieben07
 */
public enum ModUpdateState {

	AVAILABLE,
	REFRESHING,
	INSTALLING;

	public boolean canTransition(ModUpdateState state) {
		return (this == AVAILABLE) != (state == AVAILABLE);
	}

}
