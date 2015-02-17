package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
final class MethodTypeSpec<T> extends AbstractTypeSpec<T, Method> {

    MethodTypeSpec(Method member) {
        super(member);
    }

    @Override
    TypeToken<?> resolveType() {
        return TypeToken.of(member.getGenericReturnType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super T> getRawType() {
        return (Class<? super T>) member.getReturnType();
    }
}
