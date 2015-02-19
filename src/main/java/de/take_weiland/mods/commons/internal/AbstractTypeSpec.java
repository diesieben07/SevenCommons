package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 * @author diesieben07
 */
abstract class AbstractTypeSpec<T, MEM extends Member & AnnotatedElement> implements TypeSpecification<T> {

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

    @Override
    public <X> TypeSpecification<X> overwriteType(final TypeToken<X> type, final SerializationMethod.Method method) {
        return new DerivedSpec<>(this, type, method);
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

    private static class DerivedSpec<X> implements TypeSpecification<X> {

        private final TypeSpecification<?> parent;
        private final TypeToken<X> type;
        private final SerializationMethod.Method method;

        public DerivedSpec(TypeSpecification<?> parent, TypeToken<X> type, SerializationMethod.Method method) {
            this.parent = parent;
            this.type = type;
            this.method = method;
        }

        @Override
        public TypeToken<X> getType() {
            return type;
        }

        @Override
        public Class<? super X> getRawType() {
            return type.getRawType();
        }

        @Override
        public SerializationMethod.Method getDesiredMethod() {
            return method == null ? parent.getDesiredMethod() : method;
        }

        @Override
        public boolean hasAnnotation(Class<? extends Annotation> annotation) {
            return parent.hasAnnotation(annotation);
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            return parent.getAnnotation(annotationClass);
        }

        @Override
        public <X1> TypeSpecification<X1> overwriteType(TypeToken<X1> type, SerializationMethod.Method method) {
            return new DerivedSpec<>(this, type, method);
        }

        @Override
        public String toString() {
            return "TypeSpec of type " + getType();
        }
    }
}
