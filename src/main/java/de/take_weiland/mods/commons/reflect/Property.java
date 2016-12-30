package de.take_weiland.mods.commons.reflect;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.serialize.RequestSerializationMethod;
import de.take_weiland.mods.commons.asm.ASMProperty;
import de.take_weiland.mods.commons.serialize.SerializationMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * <p>Information about a Property.</p>
 *
 * @author diesieben07
 */
public interface Property<T> extends PropertyAccess<T> {

    /**
     * <p>Get the underlying Member for this property. For a getter/setter based property this returns the getter method.</p>
     *
     * @return the Member
     */
    Member getMember();

    /**
     * <p>Get the underlying AnnotatedElement for this property. Usually this is the same object as {@link #getMember()}.</p>
     * @return an AnnotatedElement
     */
    default AnnotatedElement getAnnotatedElement() {
        Member member = getMember();
        if (member instanceof AnnotatedElement) {
            return (AnnotatedElement) member;
        } else {
            return NullAnnotatedElement.INSTANCE;
        }
    }

    default boolean isStatic() {
        return Modifier.isStatic(getMember().getModifiers());
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
    default SerializationMethod getSerializationMethod() {
        RequestSerializationMethod annotation = getAnnotation(RequestSerializationMethod.class);
        if (annotation == null) {
            return SerializationMethod.DEFAULT;
        } else {
            return annotation.value();
        }
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

    ASMProperty getASMProperty();

}
