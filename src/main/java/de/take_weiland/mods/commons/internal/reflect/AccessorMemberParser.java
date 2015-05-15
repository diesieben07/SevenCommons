package de.take_weiland.mods.commons.internal.reflect;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.AnnotationNull;
import de.take_weiland.mods.commons.reflect.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
final class AccessorMemberParser {

    static MethodHandle getTarget(Method method) {
        Getter getter = method.getAnnotation(Getter.class);
        Setter setter = method.getAnnotation(Setter.class);
        Invoke invoke = method.getAnnotation(Invoke.class);
        Construct construct = method.getAnnotation(Construct.class);

        MethodHandle target;
        try {
            try {
                if (getter != null) {
                    target = getGetterTarget(method, getter);
                } else if (setter != null) {
                    target = getSetterTarget(method, setter);
                } else if (invoke != null) {
                    target = getInvokeTarget(method, invoke);
                } else if (construct != null) {
                    target = getConstructTarget(method, construct);
                } else {
                    throw new IllegalAccessorException("Don't know what to do, no known annotation found");
                }
            } catch (IllegalAccessorException e) {
                throw e;
            } catch (Throwable e) {
                throw new IllegalAccessorException("Unexpected Exception", e);
            }
            validate(method, target);
            target = target.asType(methodType(method.getReturnType(), method.getParameterTypes()));
        } catch (IllegalAccessorException t) {
            String params = Arrays.toString(method.getParameters());
            throw new IllegalAccessorException(String.format("Failed to handle method %s%s in accessor %s: %s",
                    method.getName(), params, method.getDeclaringClass().getName(), t.getMessage()), t);
        }

        return target;
    }

    private static void validate(Method method, MethodHandle target) {
        MethodType targetType = target.type();
        int myParamCount = method.getParameterCount();
        int targetParamCount = targetType.parameterCount();
        if (myParamCount != targetParamCount) {
            throw new IllegalAccessorException(String.format("Target needs %s parameters[%s], but we have %s", targetParamCount, Arrays.toString(targetType.parameterArray()), myParamCount));
        }
        if (!isCompat(method.getReturnType(), targetType.returnType())) {
            throw new IllegalAccessorException("Return type not compatible with target return type");
        }

        Class<?>[] myParams = method.getParameterTypes();
        for (int i = 0; i < myParamCount; i++) {
            if (!isCompat(myParams[i], targetType.parameterType(i))) {
                throw new IllegalAccessorException(String.format("Parameter type %s does not match up", i));
            }
        }
    }

    private static boolean isCompat(Class<?> a, Class<?> b) {
        return !(a.isPrimitive() || b.isPrimitive()) || a == b;
    }

    private static MethodHandle getGetterTarget(Method method, Getter getter) throws IllegalAccessException {
        Class<?> targetClass = getTargetClass(method, getParameterTypeOpt(method, 0), getter.target());
        Field field = getTargetField(targetClass, getter.field(), getter.srg());
        if (!method.getReturnType().isAssignableFrom(field.getType())) {
            throw new IllegalAccessorException("Field type incompatible with specified return type");
        }
        field.setAccessible(true);
        return publicLookup().unreflectGetter(field);
    }

    private static MethodHandle getSetterTarget(Method method, Setter setter) throws IllegalAccessException {
        Class<?> targetClass = getTargetClass(method, getParameterTypeOpt(method, 0), setter.target());
        Field field = getTargetField(targetClass, setter.field(), setter.srg());
        if (!field.getType().isAssignableFrom(getParameterType(method, method.getParameterCount() - 1))) {
            throw new IllegalAccessorException("Field type incompatible with specified parameter type");
        }
        field.setAccessible(true);
        return publicLookup().unreflectSetter(field);
    }

    private static Field getTargetField(Class<?> targetClass, String name, boolean srg) {
        String fieldName = srg ? MCPNames.field(name) : name;
        try {
            return targetClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalAccessorException("Could not find target field " + fieldName + " in " + targetClass.getName());
        }
    }

    private static MethodHandle getInvokeTarget(Method method, Invoke invoke) throws IllegalAccessException {
        Class<?> targetClass = getTargetClass(method, getParameterTypeOpt(method, 0), invoke.target());
        Method targetMethod = getTargetMethod(method, targetClass, invoke.method(), invoke.srg());
        if (!method.getReturnType().isAssignableFrom(targetMethod.getReturnType())) {
            throw new IllegalAccessorException("Method return type incompatible with specified return type");
        }
        targetMethod.setAccessible(true);
        return publicLookup().unreflect(targetMethod);
    }

    private static Method getTargetMethod(Method method, Class<?> targetClass, String name, boolean srg) {
        String methodName = srg ? MCPNames.method(name) : name;
        Class<?>[] paramsWithoutFirst = getParameterTypes(method, 1);
        try {
            Method instanceMethod = targetClass.getDeclaredMethod(methodName, paramsWithoutFirst);
            if (!Modifier.isStatic(instanceMethod.getModifiers())) {
                return instanceMethod;
            }
        } catch (NoSuchMethodException ignored) {
        }

        Class<?>[] params = getParameterTypes(method, 0);
        try {
            Method staticMethod = targetClass.getDeclaredMethod(methodName, params);
            if (Modifier.isStatic(staticMethod.getModifiers())) {
                return staticMethod;
            }
        } catch (NoSuchMethodException ignored) {
        }
        throw new IllegalAccessorException("No suitable target method found");
    }

    private static MethodHandle getConstructTarget(Method method, Construct construct) throws IllegalAccessException {
        Class<?> targetClass = getTargetClass(method, Optional.of(method.getReturnType()), AnnotationNull.class);
        try {
            Constructor<?> constructor = targetClass.getDeclaredConstructor(getParameterTypes(method, 0));
            constructor.setAccessible(true);
            return publicLookup().unreflectConstructor(constructor);
        } catch (NoSuchMethodException e) {
            throw new IllegalAccessorException("Target constructor not found");
        }
    }

    private static Class<?> getTargetClass(Method method, Optional<Class<?>> defaultTarget, Class<?> targetFromAnnotation) {
        OverrideTarget otAnnotation = method.getAnnotation(OverrideTarget.class);
        if (otAnnotation != null) {
            return resolveOverrideTarget(null, otAnnotation);
        }

        if (targetFromAnnotation != AnnotationNull.class) {
            return targetFromAnnotation;
        } else if (defaultTarget.isPresent() && !defaultTarget.get().isPrimitive()) {
            return defaultTarget.get();
        } else {
            throw new IllegalAccessorException("No target class specified");
        }
    }

    private static Optional<Class<?>> getParameterTypeOpt(Method method, int parameter) {
        return method.getParameterCount() <= parameter ? Optional.empty() : Optional.of(getParameterType(method, parameter));
    }

    private static Class<?> getParameterType(Method method, int parameter) {
        return getParameterType0(method.getParameters()[parameter]);
    }

    private static Class<?>[] getParameterTypes(Method method, int offset) {
        Parameter[] orig = method.getParameters();
        int newLen = orig.length - offset;
        Class<?>[] newArr = new Class<?>[newLen];
        for (int i = 0; i < newLen; i++) {
            newArr[i] = getParameterType0(orig[i + offset]);
        }
        return newArr;
    }

    private static Class<?> getParameterType0(Parameter param) {
        return resolveOverrideTarget(param.getType(), param.getAnnotation(OverrideTarget.class));
    }

    private static Class<?> resolveOverrideTarget(Class<?> defaultClass, OverrideTarget annotation) {
        if (annotation == null) {
            return defaultClass;
        }
        try {
            return Class.forName(annotation.value());
        } catch (ClassNotFoundException e) {
            throw new IllegalAccessorException("Overridden target class " + annotation.value() + " not found");
        }
    }

}
