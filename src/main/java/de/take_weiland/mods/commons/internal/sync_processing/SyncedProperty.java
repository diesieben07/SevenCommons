package de.take_weiland.mods.commons.internal.sync_processing;

import de.take_weiland.mods.commons.ExplicitSetter;
import de.take_weiland.mods.commons.sync.Sync;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * @author diesieben07
 */
public final class SyncedProperty {

    private final Element getter, setter;
    private final CompileTimeSyncerFactory syncerFactory;
    private CompileTimeSyncer syncer;


    private SyncedProperty(Element getter, Element setter, CompileTimeSyncerFactory syncerFactory) {
        this.getter = getter;
        this.setter = setter;
        this.syncerFactory = syncerFactory;
    }

    public boolean isMethod() {
        return getter.getKind() == ElementKind.METHOD;
    }

    public Element getGetter() {
        return getter;
    }

    public Element getSetter() {
        return setter;
    }

    public String getCompanionFieldName() {
        return getter.getSimpleName() + "$companion" + (isMethod() ? "$m" : "");
    }

    public CompileTimeSyncer getSyncer() {
        return syncer;
    }

    public boolean getInContainer() {
        return getter.getAnnotation(Sync.class).inContainer();
    }

    boolean isReady() {
        if (syncer == null) {
            tryInitSyncer();
        }
        return syncer != null;
    }

    private void tryInitSyncer() {
        Optional<CompileTimeSyncer> candidate = syncerFactory.getSyncer(getter.asType());
        if (candidate.isPresent()) {
            syncer = candidate.get();
        }
    }

    public static SyncedProperty create(SyncAnnotationProcessor processor, Element element) {
        if (element.getKind() == ElementKind.FIELD) {
            return new SyncedProperty(element, null, processor.getSyncerFactory());
        } else {
            checkArgument(element.getKind() == ElementKind.METHOD);
            return new SyncedProperty(element, findSetter(processor.getEnv(), (ExecutableElement) element).orElse(null), processor.getSyncerFactory());
        }
    }

    private static Optional<ExecutableElement> findSetter(ProcessingEnvironment env, ExecutableElement getter) {
        Optional<ExecutableElement> explicitSetter = findExplicitSetter(env, getter);
        if (explicitSetter.isPresent()) {
            return explicitSetter;
        } else {
            return findImplicitSetter(getter);
        }
    }

    private static Optional<ExecutableElement> findExplicitSetter(ProcessingEnvironment env, ExecutableElement getter) {
        ExplicitSetter annotation = getter.getAnnotation(ExplicitSetter.class);
        if (annotation == null) {
            return Optional.empty();
        } else {
            List<ExecutableElement> candidates = getter.getEnclosingElement().getEnclosedElements().stream()
                    .filter(e -> e.getKind() == ElementKind.METHOD)
                    .map(ExecutableElement.class::cast)
                    .filter(e -> e.getSimpleName().contentEquals(annotation.value()))
                    .filter(SyncedProperty::isSetter)
                    .filter(e -> isGetterSetterCompatible(env, getter.getReturnType(), e.getParameters().get(0).asType()))
                    .collect(Collectors.toList());

            if (candidates.size() == 1) {
                return Optional.of(candidates.get(0));
            } else {
                AnnotationMirror annotationMirror = getAnnotationMirror(getter, ExplicitSetter.class);
                throw new ProcessingException(m -> {
                    String msg;
                    if (candidates.size() == 0) {
                        msg = String.format("No valid setter method found with name %s", annotation.value());
                    } else {
                        msg = String.format("Setter name %s is ambiguous, all of %s apply", annotation.value(), candidates);
                    }
                    m.printMessage(ERROR, msg, getter, annotationMirror);
                });
            }
        }
    }

    private static AnnotationMirror getAnnotationMirror(AnnotatedConstruct element, Class<? extends Annotation> clazz) {
        String annotationName = clazz.getName();
        return element.getAnnotationMirrors().stream()
                .filter(mirror -> ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotationName))
                .findFirst()
                .orElse(null);
    }

    private static boolean isGetterSetterCompatible(ProcessingEnvironment environment, TypeMirror getterType, TypeMirror setterType) {
        return environment.getTypeUtils().isAssignable(getterType, setterType);
    }

    private static Optional<ExecutableElement> findImplicitSetter(ExecutableElement getter) {
        Element clazz = getter.getEnclosingElement();

        String propertyName = getPropertyName(getter);

        List<ExecutableElement> candidates = clazz.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .filter(SyncedProperty::isSetter)
                .filter(e -> getSetterPropertyName(e).equals(propertyName))
                .collect(Collectors.toList());

        switch (candidates.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(candidates.get(0));
            default:
                throw new ProcessingException(m -> {
                    String msg = String.format("Setter for %s is ambiguous, all of %s apply", getter, candidates);
                    m.printMessage(ERROR, msg, getter);
                });
        }
    }

    private static String getPropertyName(ExecutableElement getter) {
        String name = getter.getSimpleName().toString();
        if (name.startsWith("get")) {
            return name.substring(3);
        } else if (name.startsWith("is")) {
            return name.substring(2);
        } else {
            return name;
        }
    }

    private static String getSetterPropertyName(ExecutableElement element) {
        String name = element.getSimpleName().toString();
        return name.substring(name.startsWith("set") ? 3 : 0);
    }

    private static boolean isSetter(ExecutableElement method) {
        return method.getParameters().size() == 1 && method.getReturnType().getKind() == TypeKind.VOID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncedProperty{");
        sb.append("getter=").append(getter);
        sb.append('}');
        return sb.toString();
    }
}
