package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
public final class MethodContext<T> extends MemberContext<T, Method> {

    public MethodContext(Method member) {
        super(member);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TypeToken<T> resolveGenericType() {
        return (TypeToken<T>) TypeToken.of(member.getGenericReturnType());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super T> getRawType() {
        return (Class<? super T>) member.getReturnType();
    }
}
