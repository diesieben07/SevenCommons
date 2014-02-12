package de.take_weiland.mods.commons.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark that this Field, also annotated with {@link GetProperty @GetProperty}, represents an Item ID<br>
 * The effect of this is that {@link net.minecraftforge.common.Configuration#getItem(String, String, int, String) Configuration.getItem} will be used instead<br>
 * Also makes the category of this Property default to {@link net.minecraftforge.common.Configuration#CATEGORY_ITEM CATEGORY_ITEM} rather than {@link net.minecraftforge.common.Configuration#CATEGORY_GENERAL CATEGORY_GENERAL}
 * @author diesieben07
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Item {

}
