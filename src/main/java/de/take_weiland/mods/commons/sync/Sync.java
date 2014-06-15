package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.AnnotationNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply this annotation to a field to automatically synchronize it between Server & Client<br>
 * This only works for fields in Entities, TileEntities, Containers and instances of IExtendedEntityProperties<br>
 * Supported field types are<ul>
 *     <li>all primitives</li>
 *     <li>Enums</li>
 *     <li>Strings</li>
 *     <li>ItemStacks</li>
 *     <li>FluidStacks</li>
 *     <li>FluidTanks (<i>not</i> IFluidTank!)</li>
 * </ul>
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Sync {

	/**
	 * define the {@link TypeSyncer} to use with for this field<br>
	 * Only supported for non-primitive fields.<br>
	 * The class should either define a default (no-arg) constructor or a one-argument constructor that takes an Object<br />
	 * If the latter is present, it will be picked instead and passed the object being synced (Container, TileEntity, etc.)
	 */
	Class<? extends TypeSyncer<?>> syncer() default AnnotationNull.class;

}
