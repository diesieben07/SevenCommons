package de.take_weiland.mods.commons.internal.updater;

public enum ModUpdateState {

	/**
	 * the default state for every mod 
	 */
	LOADING {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == AVAILABLE || state == UNAVAILABLE;
		}
		
	},
	
	AVAILABLE {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
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
	 * checking for updates (downloading version info)
	 */
	CHECKING {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING_FAILED || state == UP_TO_DATE || state == UPDATES_AVAILABLE || state == MINECRAFT_OUTDATED;
		}
		
	},
	/**
	 * failed downloading the version info
	 */
	CHECKING_FAILED {

		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * mod is up to date
	 */
	UP_TO_DATE {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * updates are available for this mod which can be auto-installed
	 */
	UPDATES_AVAILABLE {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING || state == DOWNLOADING;
		}
		
	},
	/**
	 * there are updates available, but they are for a newer minecraft version
	 */
	MINECRAFT_OUTDATED {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * in progress of downloading the new version
	 */
	DOWNLOADING {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == DOWNLOAD_FAILED || state == PENDING_RESTART;
		}
		
	},
	/**
	 * failed downloading the new version
	 */
	DOWNLOAD_FAILED {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == DOWNLOADING || state == CHECKING;
		}
		
	},
	/**
	 * installed successfully, pending a minecraft restart
	 */
	PENDING_RESTART;
	
	public ModUpdateState transition(ModUpdateState desiredState) {
		if (canTransition(desiredState)) {
			return desiredState;
		} else {
			return this;
		}
	}
	
	public boolean canTransition(ModUpdateState state) {
		return false;
	}
}
