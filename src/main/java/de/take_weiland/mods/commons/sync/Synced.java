package de.take_weiland.mods.commons.sync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Synced {

	int useSyncer() default -1;
	
	int syncGroup() default -1;
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	static @interface DefineSyncer {
		
		int value();
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	static @interface SyncGroupHandler {
		
		int syncGroup();
		
	}
}
