package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.AnnotationNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>When present on a Field or Getter in a class synchronizes the contents of that property from server to client.</p>
 *
 * <p>The following types supported by default:</p>
 * <ul>
 *     <li>All primitives and their boxed versions</li>
 *     <li>String</li>
 *     <li>All enums</li>
 *     <li>UUID</li>
 *     <li>EnumSet</li>
 *     <li>BitSet</li>
 *     <li>FluidTank (not IFluidTank!)</li>
 *     <li>FluidStack</li>
 *     <li>ItemStack</li>
 * </ul>
 *
 * <p>Support for different types can be added through the {@link de.take_weiland.mods.commons.sync.SyncSupport} class.</p>
 *
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD })
public @interface Sync {

	/**
	 * <p>Override the type of the property. This will be used instead of the declared type to determine
	 * how to sync the property.</p>
	 * @return the type
	 */
	Class<?> as() default AnnotationNull.class;

	/**
	 * <p>Specify a Watcher to use for this property. The Watcher must have a
	 * {@link de.take_weiland.mods.commons.sync.WatcherFactory} annotation.</p>
	 * @return the watcher
	 */
	Class<? extends Watcher<?>> with() default AnnotationNull.class;

	/**
	 * <p>Specify how the property should be synced.</p>
	 * <ul>
	 *     <li>{@code Method.DEFAULT} picks the most appropriate of {@code Method.VALUE} and {@code Method.CONTENTS}
	 *     for the type.</li>
	 *     <li>{@code Method.VALUE} always treats the property as a value. To apply a new value, the field value will be set.</li>
	 *     <li>{@code Method.CONTENTS} treats the property as a container, like a {@code FluidTank}. The property will never be
	 *     changed and {@code null} is not valid. Instead the data inside the container will be set.</li>
	 * </ul>
	 * <p>It is up to the implementation of the Watcher if both {@code VALUE} and {@code CONTENTS} are supported or only one
	 * of the two. The meaning of {@code DEFAULT} is also up to the implementation to decide.</p>
	 * @return the method
	 */
	Method method() default Method.DEFAULT;

	/**
	 * <p>Methods of syncing.</p>
	 * @see Sync#method()
	 */
	enum Method {

		DEFAULT,
		VALUE,
		CONTENTS
	}

}
