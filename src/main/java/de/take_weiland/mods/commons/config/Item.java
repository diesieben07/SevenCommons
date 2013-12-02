package de.take_weiland.mods.commons.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.common.Configuration;

/**
 * Mark that this Field, also annotated with {@link GetProperty @GetProperty}, represents an Item ID<br>
 * The effect of this is that {@link Configuration#getItem(String, String, int, String) Configuration.getItem} will be used instead<br>
 * Also makes the category of this Property default to {@link Configuration#CATEGORY_ITEM} rather than {@link Configuration#CATEGORY_GENERAL}
 * @author diesieben07
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Item {

}
