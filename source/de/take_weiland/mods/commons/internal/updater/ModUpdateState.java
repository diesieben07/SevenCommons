package de.take_weiland.mods.commons.internal.updater;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

public enum ModUpdateState {

	/**
	 * the default state for every mod 
	 */
	LOADING("loading") {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == AVAILABLE || state == UNAVAILABLE;
		}
		
	},
	
	AVAILABLE("available") {
		
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
	UNAVAILABLE("unavailable"),
	/**
	 * checking for updates (downloading version info)
	 */
	CHECKING("checking") {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING_FAILED || state == UP_TO_DATE || state == UPDATES_AVAILABLE || state == MINECRAFT_OUTDATED;
		}
		
	},
	/**
	 * failed downloading the version info
	 */
	CHECKING_FAILED("checkingFailed") {

		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * mod is up to date
	 */
	UP_TO_DATE("upToDate") {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * updates are available for this mod which can be auto-installed
	 */
	UPDATES_AVAILABLE("updatesAvailable") {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING || state == DOWNLOADING;
		}
		
	},
	/**
	 * there are updates available, but they are for a newer minecraft version
	 */
	MINECRAFT_OUTDATED("minecraftOutdated") {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == CHECKING;
		}
		
	},
	/**
	 * in progress of downloading the new version
	 */
	DOWNLOADING("downloading") {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == DOWNLOAD_FAILED || state == PENDING_RESTART;
		}
		
	},
	/**
	 * failed downloading the new version
	 */
	DOWNLOAD_FAILED("downloadFailed") {
		
		@Override
		public boolean canTransition(ModUpdateState state) {
			return state == DOWNLOADING || state == CHECKING;
		}
		
	},
	/**
	 * installed successfully, pending a minecraft restart
	 */
	PENDING_RESTART("pendingRestart");
	
	private final String langKey;
	
	private ModUpdateState(String langKey) {
		this.langKey = langKey;
	}
	
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
	
	public String getShortDescription() {
		return I18n.getString("sevencommons.updates.state." + langKey);
	}
	
	public String getLongDescription() {
		return I18n.getString("sevencommons.updates.state." + langKey + ".long");
	}
	
	public EnumChatFormatting getDescriptionColor() {
		switch (this) {
		case LOADING:
		case AVAILABLE:
		case CHECKING:
		case DOWNLOADING:
			return EnumChatFormatting.AQUA;
		case CHECKING_FAILED:
		case DOWNLOAD_FAILED:
		case UNAVAILABLE:
			return EnumChatFormatting.RED;
		case MINECRAFT_OUTDATED:
		case UPDATES_AVAILABLE:
		case PENDING_RESTART:
			return EnumChatFormatting.YELLOW;
		case UP_TO_DATE:
		default:
			return EnumChatFormatting.GREEN;
		}
	}
}
