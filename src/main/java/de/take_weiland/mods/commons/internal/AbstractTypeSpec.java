package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
public abstract class AbstractTypeSpec<T, MEM extends Member & AnnotatedElement> implements TypeSpecification<T> {

    public static <T> TypeSpecification<T> getSpec(Member member) {
        if (member instanceof Field) {
            return new FieldTypeSpec<>((Field) member);
        } else if (member instanceof Method) {
            return new MethodTypeSpec<>((Method) member);
        } else {
            throw new IllegalArgumentException();
        }
    }

    final MEM member;
    private final SerializationMethod.Method desiredMethod;
    private TypeToken<T> genericType;

    AbstractTypeSpec(MEM member) {
        this.member = member;
        SerializationMethod annotation = member.getAnnotation(SerializationMethod.class);
        desiredMethod = annotation == null ? SerializationMethod.Method.DEFAULT : annotation.value();
    }

    @Override
    public SerializationMethod.Method getDesiredMethod() {
        return desiredMethod;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final TypeToken<T> getType() {
        return genericType == null ? (genericType = (TypeToken<T>) resolveType()) : genericType;
    }

    abstract TypeToken<?> resolveType();

    @Override
    public final <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return member.getAnnotation(annotationClass);
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return member.isAnnotationPresent(annotation);
    }

    @Override
    public String toString() {
        return "TypeSpec of type " + getType();
    }

}