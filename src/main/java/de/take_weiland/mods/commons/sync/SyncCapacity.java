package de.take_weiland.mods.commons.sync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used together with {@link de.take_weiland.mods.commons.sync.SyncContents}. If this annotation is present on a
 * FluidTank property, the capacity for the FluidTank will be synchronized in addition to just it's contained FluidStack.</p>
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD })
public @interface SyncCapacity { }
