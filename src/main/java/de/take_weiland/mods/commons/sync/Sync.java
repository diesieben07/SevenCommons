package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.serialize.SerializationMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>When present on a Field or Getter in a class synchronizes the contents of that property from server to client.</p>
 * <p>The following types supported by default:</p>
 * <ul>
 * <li>All primitives and their boxed versions</li>
 * <li>String</li>
 * <li>All enums</li>
 * <li>UUID</li>
 * <li>EnumSet</li>
 * <li>BitSet</li>
 * <li>FluidTank (not IFluidTank!)</li>
 * <li>FluidStack</li>
 * <li>ItemStack</li>
 * <li>Item and Block</li>
 * </ul>
 * <p>New supported types may be added by implementing the {@link TypeSyncer} interface.</p>
 *
 * @see TypeSyncer
 *
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Sync {

    /**
     * <p>Specify how the property should be synced.</p>
     * <ul>
     * <li>{@code Method.DEFAULT} picks the most appropriate of {@code Method.VALUE} and {@code Method.CONTENTS}
     * for the type.</li>
     * <li>{@code Method.VALUE} always treats the property as a value. To apply a new value, the field value will be set.</li>
     * <li>{@code Method.CONTENTS} treats the property as a container, like a {@code FluidTank}. The property will never be
     * changed and {@code null} is not valid. Instead the data inside the container will be set.</li>
     * </ul>
     * <p>It is up to the implementation of the Watcher if both {@code VALUE} and {@code CONTENTS} are supported or only one
     * of the two. The meaning of {@code DEFAULT} is also up to the implementation to decide.</p>
     *
     * @return the method
     */
    SerializationMethod method() default SerializationMethod.DEFAULT;

    /**
     * <p>Set this to true if the given property should be synced as a Container property when the synced object is viewed
     * as an inventory in a {@link net.minecraft.inventory.Container}.</p>
     *
     * @return true if this is a container property
     */
    boolean inContainer() default false;

}
