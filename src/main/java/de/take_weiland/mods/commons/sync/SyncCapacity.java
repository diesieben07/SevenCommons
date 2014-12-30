package de.take_weiland.mods.commons.sync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>When present on a {@linkplain de.take_weiland.mods.commons.sync.Sync synced} property of type
 * {@code FluidTank}, will also synchronize the tank's capacity.</p>
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface SyncCapacity {
}
