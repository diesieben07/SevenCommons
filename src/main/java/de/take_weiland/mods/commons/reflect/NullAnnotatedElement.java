package de.take_weiland.mods.commons.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @author diesieben07
 */
enum NullAnnotatedElement implements AnnotatedElement {
    INSTANCE;

    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return EMPTY_ANNOTATION_ARRAY;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return EMPTY_ANNOTATION_ARRAY;
    }


}
