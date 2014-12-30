package de.take_weiland.mods.commons.sync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used in a class implementing {@link de.take_weiland.mods.commons.sync.Watcher} to mark an instance factory.</p>
 * <p>When present on a field, the value of the field will be used.</p>
 * <p>When present on a method, that method must return a Watcher or null. The method must either take 0 or 1 parameter.
 * If it takes 1 parameter, a {@link de.take_weiland.mods.commons.sync.PropertyMetadata} will be passed that describes the
 * property.</p>
 *
 * @author diesieben07
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface WatcherFactory {

	/**
	 * <p>The sync methods this factory handles.</p>
	 * @return methods
	 */
	Sync.Method[] methods();

}
