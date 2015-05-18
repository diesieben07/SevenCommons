package de.take_weiland.mods.commons.reflect;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 * <p>Information about a Property.</p>
 *
 * @author diesieben07
 */
public interface Property<T, M extends AccessibleObject & Member & AnnotatedElement> extends PropertyAccess<T> {

    /**
     * <p>Get the underlying Member for this property. For a getter/setter based property this returns the getter method.</p>
     *
     * @return the Member
     */
    M getMember();

    /**
     * <p>Get a MethodHandle that gets this property.</p>
     *
     * @return a MethodHandle
     */
    MethodHandle getGetter();

    /**
     * <p>Get a MethodHandle that sets this property.</p>
     *
     * @return a MethodHandle
     */
    MethodHandle getSetter();

    @Override
    default T get(Object o) {
        try {
            //noinspection unchecked
            return (T) getGetter().invoke(o);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    default void set(Object o, T val) {
        try {
            getSetter().invoke(o, val);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    default PropertyAccess<T> optimize() {
        return this;
    }

    /**
     * <p>The name of this property.</p>
     *
     * @return the name
     */
    String getName();

    /**
     * <p>Get a TypeToken representing the type of the property.</p>
     *
     * @return a TypeToken
     */
    TypeToken<T> getType();

    /**
     * <p>Get the raw type of the property. The raw type of {@code List&lt;String&gt;} is {@code List}.</p>
     *
     * @return the raw type
     */
    Class<? super T> getRawType();

    /**
     * <p>Get the desired SerializationMethod for the property.</p>
     *
     * @return the SerializationMethod
     */
    SerializationMethod.Method getDesiredMethod();

    /**
     * <p>True, if the given annotation is present on the property.</p>
     *
     * @param annotation the annotation
     * @return if the annotation is present
     */
    boolean hasAnnotation(Class<? extends Annotation> annotation);

    /**
     * <p>Get the annotation for the specified annotation type if is present, otherwise null.</p>
     *
     * @param annotationClass the annotation type
     * @return the annotation
     */
    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

}
