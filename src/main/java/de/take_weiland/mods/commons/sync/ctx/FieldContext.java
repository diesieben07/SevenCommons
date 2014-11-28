package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
public final class FieldContext<T> extends MemberContext<T, Field> {

    public FieldContext(Field field) {
        super(field);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TypeToken<T> resolveGenericType() {
        return (TypeToken<T>) TypeToken.of(member.getGenericType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super T> getRawType() {
        return (Class<? super T>) member.getType();
    }

}
