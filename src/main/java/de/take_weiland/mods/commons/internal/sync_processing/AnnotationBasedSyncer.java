package de.take_weiland.mods.commons.internal.sync_processing;

import de.take_weiland.mods.commons.internal.sync.ChangedValue;
import de.take_weiland.mods.commons.sync.EqualityCheck;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
public class AnnotationBasedSyncer extends CompileTimeSyncer {

    private final TypeElement targetClass;
    private final ExecutableElement equalsCheck;

    private AnnotationBasedSyncer(TypeElement targetClass, ExecutableElement equalsCheck) {
        this.targetClass = targetClass;
        this.equalsCheck = equalsCheck;
    }

    public static CompileTimeSyncer createFromClass(TypeElement type) {
        ExecutableElement equalityCheck = type.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .filter(element -> isAnnotationPresent(element, EqualityCheck.class))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);

        return new AnnotationBasedSyncer(type, equalityCheck);
    }

    private static boolean isAnnotationPresent(AnnotatedConstruct element, Class<? extends Annotation> annotation) {
        return element.getAnnotationMirrors().stream()
                .map(AnnotationMirror::getAnnotationType)
                .map(DeclaredType::asElement)
                .map(TypeElement.class::cast)
                .map(TypeElement::getQualifiedName)
                .anyMatch(name -> name.contentEquals(annotation.getName()));
    }

    @Override
    public ExecutableElement getEqualityCheck() {
        return equalsCheck;
    }

    @Override
    public TypeElement getTargetClass() {
        return targetClass;
    }

    @Override
    public boolean supports(TypeMirror typeToSync) {
        TypeMirror syncingType = equalsCheck.getParameters().get(0).asType();
        return SyncAnnotationProcessor.instance().getEnv().getTypeUtils().isAssignable(typeToSync, syncingType);
    }

    @Override
    public TypeMirror getCompanionType() {
        return equalsCheck.getParameters().get(1).asType();
    }

    @Override
    public Class<? extends ChangedValue> getChangedValueClass() {
        TypeMirror type = equalsCheck.getParameters().get(0).asType();
        TypeKind kind = type.getKind();
        if (!kind.isPrimitive()) {
            return ChangedValue.OfRef.class;
        } else if (kind == TypeKind.LONG || kind == TypeKind.DOUBLE) {
            return ChangedValue.OfLong.class;
        } else {
            return ChangedValue.OfInt.class;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnnotationBasedSyncer{");
        sb.append("targetClass=").append(targetClass);
        sb.append(", equalsCheck=").append(equalsCheck);
        sb.append('}');
        return sb.toString();
    }
}
