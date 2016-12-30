package de.take_weiland.mods.commons.internal.reflect;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author diesieben07
 */
public class InterfaceProxyCompiler<T> {

    private final TypeToken<T> targetInterface;
    private final Class<? super T> rawType;
    private final List<Pair<Method, MethodHandle>> implementations = new ArrayList<>();
//    private final Map

    public InterfaceProxyCompiler(Class<T> targetInterface) {
        this(TypeToken.of(targetInterface));
    }

    public InterfaceProxyCompiler(TypeToken<T> targetInterface) {
        this.targetInterface = targetInterface;
        this.rawType = targetInterface.getRawType();
    }

    public InterfaceProxyCompiler<T> bind(Method method, MethodHandle impl) {
        Invokable<T, ?> invokable = targetInterface.method(method);
        List<TypeToken<?>> parameters = getCompleteParameterList(invokable);

        if (!checkSignatureMatch(parameters, invokable.getReturnType(), impl)) {
            throw new IllegalArgumentException("Method signatures do not match.");
        }



        return this;
    }

    private boolean checkSignatureMatch(List<TypeToken<?>> parameters, TypeToken<?> returnType, MethodHandle impl) {
        MethodType type = impl.type();
        if (type.parameterCount() != parameters.size()) {
            return false;
        }
        for (int i = 0; i < type.parameterCount(); i++) {
            TypeToken<?> declared = parameters.get(i);
            Class<?> provided = type.parameterType(i);

            // we need to pass "declared" to "provided", so declared has to be a supertype of provided
            if (!declared.isSupertypeOf(provided)) {
                return false;
            }
        }

        return returnType.isSubtypeOf(type.returnType());
    }

    private List<TypeToken<?>> getCompleteParameterList(Invokable<?, ?> invokable) {
        List<TypeToken<?>> parameters = new ArrayList<>();
        if (!invokable.isStatic()) {
            parameters.add(invokable.getOwnerType());
        }

        for (Parameter parameter : invokable.getParameters()) {
            parameters.add(parameter.getType());
        }
        return parameters;
    }

    static class Foo extends ArrayList<String> {

        @Override
        public boolean add(String s) {
            return super.add(s);
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
        String result = Arrays.stream(Foo.class.getMethods())
                .filter(m -> !m.isSynthetic())
                .sorted((o1, o2) -> o1.getDeclaringClass().isAssignableFrom(o2.getDeclaringClass()) ? -1 : 1)
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        System.out.println(result);
    }

}
