package de.take_weiland.mods.commons.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author diesieben07
 */
final class AccessorMemberParser {

    static MethodHandle getTarget(Method method) {
        Getter getter = method.getAnnotation(Getter.class);
        Setter setter = method.getAnnotation(Setter.class);
        Invoke invoke = method.getAnnotation(Invoke.class);
        Construct construct = method.getAnnotation(Construct.class);

        MethodHandle
    }

}
