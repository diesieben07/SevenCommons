package de.take_weiland.mods.commons.internal.prop;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.reflect.PropertyAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author diesieben07
 */
final class FieldProperty<T> extends AbstractProperty<T, Field> {

    FieldProperty(Field member) {
        super(member);
    }

//    @SuppressWarnings("unchecked")
//    @Override
//    PropertyAccess<T> doOptimize() {
//        return (PropertyAccess<T>) OptimizedPropertyCompiler.optimize(member);
//    }

    @Override
    public T get(Object o) {
        try {
            //noinspection unchecked
            return (T) member.get(o);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void set(Object o, T val) {
        if (Modifier.isFinal(member.getModifiers())) {
            throw immutableEx();
        }
        try {
            member.set(o, val);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    TypeToken<?> resolveType() {
        return TypeToken.of(member.getGenericType());
    }

    @Override
    public String getName() {
        return member.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super Object> getRawType() {
        return (Class<? super Object>) member.getType();
    }

}
