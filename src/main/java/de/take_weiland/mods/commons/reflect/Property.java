package de.take_weiland.mods.commons.reflect;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Optional;

/**
 * <p>Information about a Property.</p>
 *
 * @author diesieben07
 */
public interface Property<T> extends PropertyAccess<T> {

    /**
     * <p>Get the underlying Member for this property. For a getter/setter based property this returns the getter method.</p>
     * <p>This method will always return something that is also an {@link AnnotatedElement}. For convenience the method
     * {@link #getAnnotatedElement()} may be used.</p>
     *
     * @return the Member
     */
    Member getMember();

    default AnnotatedElement getAnnotatedElement() {
        try {
            return (AnnotatedElement) getMember();
        } catch (ClassCastException e) {
            throw new IllegalStateException("getMember of a Property must return an AnnotatedElement", e);
        }
    }

    /**
     * <p>Return a {@link PropertyAccess} instance that will access the same property but may possibly be faster.</p>
     *
     * @return a possibly faster PropertyAccess
     */
    PropertyAccess<T> optimize();

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
    default SerializationMethod.Method getDesiredMethod() {
        return Optional.ofNullable(getAnnotation(SerializationMethod.class))
                .map(SerializationMethod::value)
                .orElse(SerializationMethod.Method.DEFAULT);
    }

    /**
     * <p>True, if the given annotation is present on the property.</p>
     *
     * @param annotation the annotation
     * @return if the annotation is present
     */
    default boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return getAnnotatedElement().isAnnotationPresent(annotation);
    }

    /**
     * <p>Get the annotation for the specified annotation type if is present, otherwise null.</p>
     *
     * @param annotationClass the annotation type
     * @return the annotation
     */
    default <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getAnnotatedElement().getAnnotation(annotationClass);
    }

}
