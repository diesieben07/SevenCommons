package de.take_weiland.mods.commons.util.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.common.Configuration;

/**
 * Mark that this Field, also annotated with {@link GetProperty @GetProperty}, represents a Block ID<br>
 * The effect of this is that {@link Configuration#getBlock(String, String, int, String) Configuration.getBlock} will be used instead<br>
 * Also makes the category of this Property default to {@link Configuration#CATEGORY_BLOCK} rather than {@link Configuration#CATEGORY_GENERAL}
 * @author diesieben07
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Block {

	/**
	 * @return if {@link Configuration#getTerrainBlock(String, String, int, String) Configuration.getTerrainBlock} should be used for this Block ID
	 */
	public boolean isTerrain() default false;
	
}
