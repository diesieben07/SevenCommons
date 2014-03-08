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
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Synced {

	/**
	 * define the {@link TypeSyncer} to use with for this field<br>
	 * Only supported for non-primitive fields.<br>
	 * The class should either define a default (no-arg) constructor or a one-argument constructor that takes an Object<br />
	 * If the latter is present, it will be picked instead and passed the object being synced (Container, TileEntity, etc.)
	 */
	Class<? extends TypeSyncer<?>> syncer() default NULL.class;

    /**
     * override where the Packets to sync this class should be sent to<br />
     * The same constructor rules as for {@link de.take_weiland.mods.commons.sync.Synced#syncer() syncer()} apply
     */
    Class<? extends PacketTarget> target() default NULL.class;

	/**
	 * When applied to a Method, define the corresponding setter method (must be in this class and marked with {@link de.take_weiland.mods.commons.sync.Synced.Setter @Setter})
	 */
	String setter() default "NULL"; // default value doesn't matter as it's read by ASM and default values are not present in an AnnotationNode

    /**
     * Dummy interface, used as default value for target() and syncer()
     */
    static interface NULL extends TypeSyncer<Object>, PacketTarget { }

	/**
	 * mark a method as a setter, needs a corresponding {@link de.take_weiland.mods.commons.sync.Synced @Synced} method with the same ID
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	static @interface Setter {

		String value();

	}

}
