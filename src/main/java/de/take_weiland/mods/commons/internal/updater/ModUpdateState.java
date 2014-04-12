package de.take_weiland.mods.commons.internal.updater;

/**
 * @author diesieben07
 */
public enum ModUpdateState {

	AVAILABLE,
	REFRESHING,
	INSTALLING,
	INSTALL_OK,
	INSTALL_FAIL;

	public boolean canTransition(ModUpdateState state) {
		switch (this) {
			case INSTALL_OK:
				// in case one mod fails, we need to reset everything
				return state == AVAILABLE;
			case INSTALLING:
				return state == INSTALL_OK || state == INSTALL_FAIL;
			case REFRESHING:
				return state == AVAILABLE;
			case INSTALL_FAIL:
				return state == AVAILABLE;
			case AVAILABLE:
				return state == INSTALLING || state == REFRESHING;
		}
		throw new IllegalArgumentException("unpossible");
	}

}
