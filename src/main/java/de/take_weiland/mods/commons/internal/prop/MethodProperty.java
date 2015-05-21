package de.take_weiland.mods.commons.internal.prop;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.apache.commons.lang3.StringUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * @author diesieben07
 */
final class MethodProperty<T> extends AbstractProperty<T, Method> {

    private final Method setter;

    MethodProperty(Method member) {
        super(member);
        setter = SCReflection.findSetter(member);
        if (setter != null) {
            setter.setAccessible(true);
        }
    }

    @Override
    public T get(Object o) {
        try {
            //noinspection unchecked
            return (T) member.invoke(o);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public void set(Object o, T val) {
        if (setter == null) {
            throw new UnsupportedOperationException("No setter");
        }
        try {
            setter.invoke(o, val);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    MethodHandle resolveGetter() throws IllegalAccessException {
        return publicLookup().unreflect(member);
    }

    @Override
    MethodHandle resolveSetter() throws IllegalAccessException {
        Method setter = SCReflection.findSetter(member);
        if (setter == null) {
            return null;
        } else {
            setter.setAccessible(true);
            return publicLookup().unreflect(setter);
        }
    }

    @Override
    TypeToken<?> resolveType() {
        return TypeToken.of(member.getGenericReturnType());
    }

    @Override
    public String getName() {
        String name = member.getName();
        if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
            return StringUtils.uncapitalize(name.substring(3));
        } else {
            return name;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super T> getRawType() {
        return (Class<? super T>) member.getReturnType();
    }
}
