package de.take_weiland.mods.commons.internal.sync_processing.description;

import com.google.common.collect.Iterators;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.*;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static javax.lang.model.util.ElementFilter.fieldsIn;

/**
 * @author diesieben07
 */
public interface ObjectDescription<T> {

    T resolve(ProcessingEnvironment env);

    static ObjectDescription<TypeElement> forClass(TypeElement element) {
        String name = element.getQualifiedName().toString();
        return env -> env.getElementUtils().getTypeElement(name);
    }

    static ObjectDescription<VariableElement> forVariable(VariableElement element) {
        ElementKind kind = element.getKind();
        String name = element.getSimpleName().toString();
        ObjectDescription<TypeMirror> type = forType(element.asType());

        switch (kind) {
            case FIELD:
            case ENUM_CONSTANT:
                ObjectDescription<TypeElement> containingClass = forClass((TypeElement) element.getEnclosingElement());
                return env -> {
                    TypeMirror resolvedType = type.resolve(env);

                    return fieldsIn(containingClass.resolve(env).getEnclosedElements()).stream()
                            .filter(e -> e.getSimpleName().contentEquals(name))
                            .filter(e -> env.getTypeUtils().isSameType(e.asType(), resolvedType))
                            .findAny()
                            .orElseThrow(IllegalStateException::new);
                };
            case PARAMETER:
                ObjectDescription<ExecutableElement> containingMethod = forMethod((ExecutableElement) element.getEnclosingElement());
                return env -> {
                    TypeMirror resolvedType = type.resolve(env);
                    return Iterators.getOnlyElement(containingMethod.resolve(env).getParameters().stream()
                            .filter(e -> e.getSimpleName().contentEquals(name))
                            .filter(e -> env.getTypeUtils().isSameType(e.asType(), resolvedType))
                            .iterator());

                };
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE:
            case EXCEPTION_PARAMETER:
                throw new UnsupportedOperationException("Cannot encode " + kind);
            default:
                throw new IllegalArgumentException();
        }
    }

    static ObjectDescription<ExecutableElement> forMethod(ExecutableElement element) {
        ElementKind kind = element.getKind();
        ObjectDescription<TypeElement> containingClass = forClass((TypeElement) element.getEnclosingElement());

        String name = element.getSimpleName().toString();
        List<ObjectDescription<TypeMirror>> parameters = element.getParameters().stream()
                .map(VariableElement::asType)
                .map(ObjectDescription::forType)
                .collect(Collectors.toList());

        ObjectDescription<TypeMirror> returnType = forType(element.getReturnType());

        return env -> {
            TypeElement clazz = containingClass.resolve(env);
            TypeMirror rt = returnType.resolve(env);
            List<TypeMirror> rp = parameters.stream().map(d -> d.resolve(env)).collect(Collectors.toList());

            return Iterators.getOnlyElement(clazz.getEnclosedElements().stream()
                    .filter(e -> e.getKind() == kind)
                    .map(ExecutableElement.class::cast)
                    .filter(e -> e.getSimpleName().contentEquals(name))
                    .filter(e -> env.getTypeUtils().isSameType(rt, e.getReturnType()))
                    .filter(e -> doParamsMatch(env, rp, e))
                    .iterator());
        };
    }

    static boolean doParamsMatch(ProcessingEnvironment env, List<TypeMirror> rp, ExecutableElement e) {
        return matchPairwise(e.getParameters().stream().map(VariableElement::asType).iterator(), rp.iterator(), env.getTypeUtils()::isSameType);
    }

    static ObjectDescription<TypeMirror> forType(TypeMirror type) {
        TypeKind kind = type.getKind();
        if (kind.isPrimitive()) {
            return env -> env.getTypeUtils().getPrimitiveType(kind);
        } else {
            switch (kind) {
                case VOID:
                case NONE:
                case NULL:
                    return env -> env.getTypeUtils().getNullType();
                case ARRAY:
                    ArrayType arrayType = (ArrayType) type;
                    ObjectDescription<TypeMirror> component = forType(arrayType.getComponentType());
                    return env -> env.getTypeUtils().getArrayType(component.resolve(env));
                case DECLARED:
                    DeclaredType declaredType = (DeclaredType) type;
                    List<ObjectDescription<TypeMirror>> typeArgs = declaredType.getTypeArguments().stream()
                            .map(ObjectDescription::forType)
                            .collect(Collectors.toList());
                    ObjectDescription<TypeElement> baseElement = forClass((TypeElement) declaredType.asElement());
                    ObjectDescription<TypeMirror> enclosingType = forType(declaredType.getEnclosingType());

                    return env -> {
                        TypeElement base = baseElement.resolve(env);
                        TypeMirror enclosing = enclosingType.resolve(env);
                        TypeMirror[] args = typeArgs.stream()
                                .map(d -> d.resolve(env))
                                .toArray(TypeMirror[]::new);

                        if (enclosing.getKind() != TypeKind.DECLARED) {
                            return env.getTypeUtils().getDeclaredType(base, args);
                        } else {
                            return env.getTypeUtils().getDeclaredType((DeclaredType) enclosing, base, args);
                        }
                    };

                case WILDCARD:
                    WildcardType wildcardType = (WildcardType) type;
                    Optional<ObjectDescription<TypeMirror>> extendsBound = Optional.ofNullable(wildcardType.getExtendsBound())
                            .map(ObjectDescription::forType);
                    Optional<ObjectDescription<TypeMirror>> superBound = Optional.ofNullable(wildcardType.getSuperBound())
                            .map(ObjectDescription::forType);

                    return env -> {
                        TypeMirror ex = extendsBound.map(d -> d.resolve(env)).orElse(null);
                        TypeMirror su = superBound.map(d -> d.resolve(env)).orElse(null);
                        return env.getTypeUtils().getWildcardType(ex, su);
                    };
                case INTERSECTION:
                case UNION:
                case ERROR:
                case TYPEVAR:
                case PACKAGE:
                case EXECUTABLE:
                case OTHER:
                    throw new UnsupportedOperationException("Cannot encode " + kind + " type");
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    static <T, R> boolean matchPairwise(Iterator<T> ai, Iterator<R> bi, BiPredicate<? super T, ? super R> check) {
        while (ai.hasNext() && bi.hasNext()) {
            T ae = ai.next();
            R be = bi.next();
            if (!check.test(ae, be)) {
                return false;
            }
        }
        return !ai.hasNext() && !bi.hasNext();
    }

}
