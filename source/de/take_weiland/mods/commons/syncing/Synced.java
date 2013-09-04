package de.take_weiland.mods.commons.syncing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Synced {

	void downloadSyncedFields();
	
	void uploadSyncedFields();
	
	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.FIELD)
	public static @interface Sync {
		
	}

}
