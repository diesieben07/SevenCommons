package de.take_weiland.mods.commons.reflect;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.AnnotationNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * @author diesieben07
 */
final class AccessorMemberParser {

    static MethodHandle getTarget(Method method) {
        try {
            MethodHandle target = getTarget0(method);
            Class<?>[] incomingArgs = method.getParameterTypes();

            checkState(target.type().parameterCount() == incomingArgs.length, "Something went wrong, got wrong method param count");
            for (int i = 0; i < incomingArgs.length; i++) {
                Class<?> inArg = incomingArgs[i];
                Class<?> outArg = target.type().parameterType(i);

                if (inArg != outArg && !outArg.isAssignableFrom(inArg)) {
                    throw new IllegalAccessorException("method parameters don't match up, cannot convert " + inArg.getName() + " to " + outArg.getName());
                }
            }
            Class<?> inRet = target.type().returnType();
            Class<?> outRet = method.getReturnType();
            if (inRet != outRet && !outRet.isAssignableFrom(inRet)) {
                throw new IllegalAccessorException("return types don't match up, cannot convert " + inRet.getName() + " to " + outRet.getName());
            }
            return target;
        } catch (Exception e) {
            String msg = String.format(
                    "Failed to handle method %s in accessor interface %s. Reason: %s",
                    method.getName(),
                    method.getDeclaringClass().getName(),
                    Objects.toString(e.getMessage(), "Unknown"));

            RuntimeException newEx = new IllegalAccessorException(msg);
            Throwable cause = e instanceof IllegalAccessorException ? e.getCause() : e;
            if (cause != null) {
                newEx.initCause(cause);
            }
            throw newEx;
        }
    }

    private static MethodHandle getTarget0(Method method) {
        Construct construct = method.getAnnotation(Construct.class);
        if (construct != null) {
            Class<?> targetClass = maybeOverrideTarget(method.getReturnType(), method.getAnnotation(OverrideTarget.class));
            checkNotPrimitive(targetClass);

            Class<?>[] actualParameters = transformParameters(method, 0);
            Constructor<?> constructor;
            try {
                constructor = targetClass.getDeclaredConstructor(actualParameters);
            } catch (NoSuchMethodException e) {
                throw new IllegalAccessorException("No such constructor found", e);
            }
            constructor.setAccessible(true);
            try {
                return publicLookup().unreflectConstructor(constructor);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("impossible");
            }
        }

        Getter getter = method.getAnnotation(Getter.class);
        if (getter != null) {
            Class<?> targetClass;
            int numParameters = method.getParameterTypes().length;
            if (numParameters == 0) {
                targetClass = getter.target();
            } else if (numParameters == 1) {
                targetClass = method.getParameterTypes()[0];
            } else {
                throw new IllegalAccessorException("@Getter must have 0 or 1 parameter");
            }
            maybeOverrideTarget(targetClass, method.getAnnotation(OverrideTarget.class));
            checkNotPrimitive(targetClass);
            if (targetClass == AnnotationNull.class) {
                throw new IllegalAccessorException("@Getter is missing a target");
            }

            Field field;
            try {
                field = targetClass.getDeclaredField(getter.srg() ? MCPNames.field(getter.field()) : getter.field());
            } catch (NoSuchFieldException e) {
                throw new IllegalAccessorException("Field target for @Getter not found", e);
            }
            if (Modifier.isStatic(field.getModifiers())) {
                if (numParameters != 0) {
                    throw new IllegalAccessorException("@Getter with static field target must not take any parameters");
                }
            } else {
                if (numParameters != 1) {
                    throw new IllegalAccessorException("@Getter with non-static field target must take one parameter");
                }
            }
            field.setAccessible(true);
            try {
                return publicLookup().unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new  RuntimeException("impossible");
            }
        }

        Setter setter = method.getAnnotation(Setter.class);
        if (setter != null) {
            Class<?> targetClass;
            int numParameters = method.getParameterTypes().length;
            if (numParameters == 1) {
                targetClass = setter.target();
            } else if (numParameters == 2) {
                targetClass = method.getParameterTypes()[0];
            } else {
                throw new IllegalAccessorException("@Setter must take 1 or 2 parameters");
            }
            maybeOverrideTarget(targetClass, method.getAnnotation(OverrideTarget.class));
            checkNotPrimitive(targetClass);
            if (targetClass == AnnotationNull.class) {
                throw new IllegalAccessorException("@Setter is missing a target");
            }

            Field field;
            try {
                field = targetClass.getDeclaredField(setter.srg() ? MCPNames.field(setter.field()) : setter.field());
            } catch (NoSuchFieldException e) {
                throw new IllegalAccessorException("Field target for @Setter not found", e);
            }
            if (Modifier.isStatic(field.getModifiers())) {
                if (numParameters != 1) {
                    throw new IllegalAccessorException("@Setter with static field target must take 1 parameter");
                }
            } else {
                if (numParameters != 2) {
                    throw new IllegalAccessorException("@Setter with non-static field target must take 2 parameters");
                }
            }
            field.setAccessible(true);
            try {
                return publicLookup().unreflectSetter(field);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("impossible");
            }
        }
        Invoke invoke = method.getAnnotation(Invoke.class);
        if (invoke != null) {
            Class<?> targetClass = invoke.target();
            int numParameters = method.getParameterTypes().length;
            boolean isStatic;
            if (targetClass == AnnotationNull.class) {
                if (numParameters == 0) {
                    throw new IllegalAccessorException("@Invoke is missing a target");
                } else {
                    targetClass = method.getParameterTypes()[0];
                }
                isStatic = false;
            } else {
                isStatic = true;
            }

            targetClass = maybeOverrideTarget(targetClass, method.getAnnotation(OverrideTarget.class));
            checkNotPrimitive(targetClass);

            Class<?>[] actualParams = transformParameters(method, isStatic ? 0 : 1);
            Method targetMethod;
            try {
                targetMethod = targetClass.getDeclaredMethod(invoke.srg() ? MCPNames.method(invoke.method()) : invoke.method(), actualParams);
            } catch (NoSuchMethodException e) {
                throw new IllegalAccessorException("Method target for @Invoke not found");
            }
            if (isStatic != Modifier.isStatic(targetMethod.getModifiers())) {
                throw new IllegalAccessorException("Expected a " + (isStatic ? "" : "non-") + "static method target for @Invoke");
            }
            targetMethod.setAccessible(true);
            try {
                return publicLookup().unreflect(targetMethod);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("impossible");
            }
        }
        throw new IllegalAccessorException("Don't know what to do with method " + method.getName());
    }

    private static Class<?>[] transformParameters(Method method, int offset) {
        Class<?>[] origParams = method.getParameterTypes();
        Class<?>[] newParams = new Class<?>[origParams.length - offset];
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = offset; i < origParams.length; i++) {
            newParams[i - offset] = maybeOverrideTarget(origParams[i], getOverrideTargetForParameter(parameterAnnotations[i]));
        }

        return newParams;
    }

    private static OverrideTarget getOverrideTargetForParameter(Annotation[] arr) {
        for (Annotation annotation : arr) {
            if (annotation instanceof OverrideTarget) {
                return (OverrideTarget) annotation;
            }
        }
        return null;
    }

    private static Class<?> maybeOverrideTarget(Class<?> target, OverrideTarget annotation) {
        if (annotation != null) {
            try {
                target = Class.forName(annotation.value());
            } catch (ClassNotFoundException e) {
                throw new IllegalAccessorException("Overridden target "+ annotation.value() + " not found", e);
            }
        }
        return target;
    }

    private static void checkNotPrimitive(Class<?> target) {
        if (target.isPrimitive()) {
            throw new IllegalAccessorException("Cannot use primitive class as target");
        }
    }

    private AccessorMemberParser() { }

}
