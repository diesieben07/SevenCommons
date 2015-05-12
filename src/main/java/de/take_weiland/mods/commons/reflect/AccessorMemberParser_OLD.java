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
final class AccessorMemberParser_OLD {

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
            return parseGetter(method, getter);
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
            return parseInvoke(method, invoke);
        }
        throw new IllegalAccessorException("Don't know what to do with method " + method.getName());
    }

    private static MethodHandle parseGetter(Method method, Getter annotation) {
        Class<?> targetClass;
        int numParameters = method.getParameterTypes().length;
        if (numParameters == 0) {
            targetClass = annotation.target();
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
            field = targetClass.getDeclaredField(annotation.srg() ? MCPNames.field(annotation.field()) : annotation.field());
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

    private static MethodHandle parseInvoke(Method method, Invoke annotation) {
        Class<?>[] parameters = method.getParameterTypes();
        Class<?> targetClass = annotation.target();
        boolean isTargetStatic = true;
        if (targetClass == AnnotationNull.class) {
            if (parameters.length == 0) {
                throw new IllegalAccessorException("Couldn't find target class for @Invoke, have 0 parameters");
            }
            targetClass = parameters[0];
            isTargetStatic = false;
        }
        targetClass = maybeOverrideTarget(targetClass, method.getAnnotation(OverrideTarget.class));
        String targetMethodName = annotation.srg() ? MCPNames.method(annotation.method()) : annotation.method();
        Class<?>[] targetMethodArgs = transformParameters(method, isTargetStatic ? 0 : 1);
        Method targetMethod = tryFindMethod(targetClass, targetMethodName, method.getReturnType(), targetMethodArgs);
        try {
            return publicLookup().unreflect(targetMethod);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("impossible");
        }
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

    private static Method tryFindMethod(Class<?> clazz, String method, Class<?> expectedReturnType, Class<?>[] args) {
        try {
            Method m = clazz.getDeclaredMethod(method, args);
            m.setAccessible(true);
            if (!expectedReturnType.isAssignableFrom(m.getReturnType())) {
                throw new IllegalAccessorException("Target method return type not convertible to accessor method return type");
            }
            return m;
        } catch (NoSuchMethodException e) {
            throw new IllegalAccessorException("Couldn't find method " + method + " with required parameter types in class " + clazz.getName());
        }
    }

    private static Field tryFindField(Class<?> clazz, String field, Class<?> expectedType, boolean willSet) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            if (willSet ? !f.getType().isAssignableFrom(expectedType) : !expectedType.isAssignableFrom(f.getType())) {
                throw new IllegalAccessorException("Target field type not convertible to type required by accessor method");
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
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

    private AccessorMemberParser_OLD() { }

}
