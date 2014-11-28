package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.collect.ImmutableMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @author diesieben07
 */
public abstract class MemberContext<T, MEM extends AnnotatedElement> extends AbstractContext<T> {

    protected final MEM member;

    protected MemberContext(MEM member) {
        this.member = member;
    }

    @Override
    protected ImmutableMap<Key<?>, Object> resolveData() {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        for (Annotation annotation : member.getAnnotations()) {
            ContextAnnotations.resolve(annotation, builder);
        }
        return builder.build();
    }
}
