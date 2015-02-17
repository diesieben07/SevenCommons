package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
final class FieldTypeSpec<T> extends AbstractTypeSpec<T, Field> {

    FieldTypeSpec(Field member) {
        super(member);
    }

    @Override
    TypeToken<?> resolveType() {
        return TypeToken.of(member.getGenericType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super Object> getRawType() {
        return (Class<? super Object>) member.getType();
    }
}
