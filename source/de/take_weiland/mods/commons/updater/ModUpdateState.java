package de.take_weiland.mods.commons.updater;

public enum ModUpdateState {

	/**
	 * the default state for every mod 
	 */
	LOADING {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING || state == UNAVAILABLE || state == DISABLED;
		}
		
	},	
	/**
	 * this mod doesn't support updating<br>
	 * reasons:
	 * <li>not a jar/zip file
	 * <li>doesn't provide an update URL
	 * <li>update URL is invalid
	 */
	UNAVAILABLE,
	/**
	 * update checking has been disabled
	 */
	DISABLED,
	/**
	 * checking for updates (downloading version info)
	 */
	CHECKING {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING_FAILED || state == UP_TO_DATE || state == UPDATES_AVAILABLE || state == CANNOT_INSTALL || state == MINECRAFT_OUTDATED;
		}
		
	},
	/**
	 * failed downloading the version info
	 */
	CHECKING_FAILED {

		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * mod is up to date
	 */
	UP_TO_DATE {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * updates are available for this mod which can be auto-installed
	 */
	UPDATES_AVAILABLE {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING || state == DOWNLOADING;
		}
		
	},
	/**
	 * updates are available for this mod, but can't be auto-installed for whatever reason
	 */
	CANNOT_INSTALL {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * there are updates available they are for a newer minecraft version
	 */
	MINECRAFT_OUTDATED {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * in progress of downloading the new version
	 */
	DOWNLOADING {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == DOWNLOAD_FAILED || state == INSTALLING;
		}
		
	},
	/**
	 * failed downloading the new version
	 */
	DOWNLOAD_FAILED {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == DOWNLOADING || state == CHECKING;
		}
		
	},
	/**
	 * installing the new version
	 */
	INSTALLING {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == INSTALL_FAILED || state == PENDING_RESTART;
		}
		
	},
	/**
	 * failed to install for whatever reason
	 */
	INSTALL_FAILED {
		
		@Override
		boolean canTransition(ModUpdateState state) {
			return state == CHECKING || state == INSTALLING || state == DOWNLOADING;
		}
		
	},
	/**
	 * installed successfully, pending a minecraft restart
	 */
	PENDING_RESTART;
	
	public ModUpdateState transition(ModUpdateState desiredState) {
		System.out.println("TRANSITION FROM " + this + " TO " + desiredState);
		if (canTransition(desiredState)) {
			return desiredState;
		} else {
			return this;
		}
	}
	
	boolean canTransition(ModUpdateState state) {
		return false;
	}
}
