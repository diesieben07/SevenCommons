package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.annotation.Annotation;

/**
* @author diesieben07
*/
public final class DerivedTypeSpec<T> implements TypeSpecification<T> {

    private final TypeSpecification<?> parent;
    private final TypeToken<T> type;
    private final SerializationMethod.Method method;

    public DerivedTypeSpec(TypeSpecification<?> parent, TypeToken<T> type, SerializationMethod.Method method) {
        this.parent = parent;
        this.type = type;
        this.method = method;
    }

    @Override
    public TypeToken<T> getType() {
        return type;
    }

    @Override
    public Class<? super T> getRawType() {
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
    public String toString() {
        return "TypeSpec of type " + getType();
    }
}
