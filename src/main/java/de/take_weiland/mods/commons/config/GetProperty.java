package de.take_weiland.mods.commons.config;

import net.minecraftforge.common.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a field with this to inject a configuration property into it.<br>
 * The field has to be static and of type int, boolean, double or String (or an array of those).<br><br>
 * Use {@link ConfigInjector#inject(net.minecraftforge.common.Configuration, Class, boolean, boolean) ConfigInjector.inject} to populate all fields having this annotation<br>
 * 
 * The field's initial value will be used as the default
 * 
 * @see Configuration
 * @author diesieben07
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GetProperty {

	/**
	 * the name of the property to inject into this field<br>
	 * defaults to the field name
	 * @return the property name
	 */
	String name() default "";
	
	/**
	 * a comment for this property<br>
	 * defaults to no comment
	 * @return a comment
	 */
	String comment() default "";
	
	/**
	 * the category for this property<br>
	 * defaults to {@link Configuration#CATEGORY_GENERAL} unless {@link Item @Item} or {@link Block @Block} are present as well
	 * @return the category
	 */
	String category() default "";
	
}
