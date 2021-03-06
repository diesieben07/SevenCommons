package de.take_weiland.mods.commons.internal.prop;

import com.google.common.reflect.TypeToken;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * @author diesieben07
 */
final class FieldProperty<T> extends AbstractProperty<T, Field> {

    FieldProperty(Field member) {
        super(member);
    }

    @Override
    MethodHandle resolveGetter() throws IllegalAccessException {
        return publicLookup().unreflectGetter(member);
    }

    @Override
    MethodHandle resolveSetter() throws IllegalAccessException {
        return publicLookup().unreflectSetter(member);
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
