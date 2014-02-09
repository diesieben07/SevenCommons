package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.PacketTarget;

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
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Synced {

	/**
	 * define the {@link TypeSyncer} to use with for this field<br>
	 * Only supported for non-primitive fields.<br>
	 */
	Class<? extends TypeSyncer<?>> syncer() default NULL.class;

    /**
     * override where the Packets to sync this class should be sent to<br />
     * The class may declare a one-argument constructor which will then be passed the appropriate instance
     */
    Class<? extends PacketTarget> target() default NULL.class;

    /**
     * Dummy interface, used as default value for target() and syncer()
     */
    static interface NULL extends TypeSyncer<Object>, PacketTarget { }
	
}
