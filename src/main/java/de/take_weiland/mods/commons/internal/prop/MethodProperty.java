package de.take_weiland.mods.commons.internal.prop;

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.asm.ASMProperty;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

//    @SuppressWarnings("unchecked")
//    @Override
//    PropertyAccess<T> doOptimize() {
//        return (PropertyAccess<T>) OptimizedPropertyCompiler.optimize(member, setter);
//    }

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
            throw immutableEx();
        }
        try {
            setter.invoke(o, val);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw Throwables.propagate(e.getCause());
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
        } else if (name.startsWith("is") && name.length() > 2 && Character.isUpperCase(name.charAt(2))) {
            return StringUtils.uncapitalize(name.substring(2));
        } else {
            return name;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super T> getRawType() {
        return (Class<? super T>) member.getReturnType();
    }

    @Override
    public ASMProperty getASMProperty() {
        return ASMProperty.forGetterSetter(member, setter);
    }
}
