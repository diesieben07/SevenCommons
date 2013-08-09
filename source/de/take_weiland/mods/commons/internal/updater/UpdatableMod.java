package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import com.google.common.collect.Ordering;

public interface UpdatableMod {

	public static final Comparator<UpdatableMod> ACTIVE_ORDER = new Comparator<UpdatableMod>() {

		@Override
		public int compare(UpdatableMod mod1, UpdatableMod mod2) {
			boolean disabled1 = mod1.getState() == ModUpdateState.UNAVAILABLE;
			boolean disabled2 = mod2.getState() == ModUpdateState.UNAVAILABLE;
			return disabled1 == disabled2 ? 0 : disabled1 ? 1 : -1;
		}
		
		
	};
	
	public static final Comparator<UpdatableMod> NAME_ORDER = new Comparator<UpdatableMod>() {

		@Override
		public int compare(UpdatableMod o1, UpdatableMod o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	@SuppressWarnings("unchecked") // dang you java 6
	public static final Comparator<UpdatableMod> ACTIVE_AND_NAME_ORDER = Ordering.compound(Arrays.asList(ACTIVE_ORDER, NAME_ORDER));
	
	public String getModId();
	
	public String getName();

	public UpdateController getController();

	public URL getUpdateURL();

	public File getSource();

	public boolean transition(ModUpdateState desiredState);

	public ModUpdateState getState();

	public ModVersionCollection getVersions();
	
	public void setDownloadProgress(int progress, int total);
	
	public int getDowloadProgress(int max);
	
}
