package de.take_weiland.mods.commons.asmproxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply this Annotation to an Interface to make it a valid ProxyInterface.<br>
 * @author diesieben07
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TargetClass {
	
	/**
	 * Get which class this ProxyInterface should be applied to
	 * @return the target classes name
	 */
	public String value();
	
}
