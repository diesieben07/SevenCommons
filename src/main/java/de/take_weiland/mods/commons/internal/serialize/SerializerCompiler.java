package de.take_weiland.mods.commons.internal.serialize;

import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.serialize.BaseSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.SerializationTarget;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.minecraft.nbt.NBTBase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.stream.IntStream;

import static net.bytebuddy.implementation.MethodCall.invoke;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * @author diesieben07
 */
public class SerializerCompiler {

    public static <T> BaseSerializer<T> compileSerializer(SerializationTarget target, SerializationMethod method, Method serializer, Method deserializer) {
        ElementMatcher<MethodDescription> readMatcher = named("read").and(takesArguments(1));
        ElementMatcher<MethodDescription> writeMatcher = named("write").and(takesArguments(1));

        TypeToken<?> genericType;
        if (deserializer.getReturnType().isPrimitive()) {
            genericType = TypeToken.of(Primitives.wrap(deserializer.getReturnType()));
        } else {
            genericType = TypeToken.of(deserializer.getGenericReturnType());
        }
        TypeToken<? extends BaseSerializer<?>> implementedInterface = target.getTypedInterface(method, genericType);

        Implementation readImpl, writeImpl;
        readImpl = invokeLenientParams(new MethodDescription.ForLoadedMethod(deserializer));
        writeImpl = invokeLenientParams(new MethodDescription.ForLoadedMethod(serializer));

        try {
            DynamicType.Unloaded<Object> clazz = new ByteBuddy(ClassFileVersion.JAVA_V8)
                    .subclass(Object.class)
                    .implement(implementedInterface.getType())
                    .method(readMatcher).intercept(readImpl)
                    .method(writeMatcher).intercept(writeImpl)
                    .make();

            try {
                clazz.saveIn(new File("C:/Users/takew/Desktop/foo"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //noinspection unchecked
            return (BaseSerializer<T>) clazz
                    .load(Thread.currentThread().getContextClassLoader())
                    .getLoaded()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // this should never happen
            throw new RuntimeException();
        }
    }

    private static Implementation invokeLenientParams(MethodDescription method) {
        MethodCall.WithoutSpecifiedTarget call = invoke(method);
        MethodCall withTarget;
        int i;
        if (!method.isStatic()) {
            withTarget = call.onArgument(0);
            i = 1;
        } else {
            withTarget = call;
            i = 0;
        }

        return withTarget.withArgument(IntStream.range(i, method.getParameters().size() - i).toArray());
    }

    public static class Test {

        public String foo;

        public Test(String foo)
        {
            this.foo = foo;
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
//        Method deserializer = MCDataInput.class.getMethod("readShort");
//        Method serializer = DefaultSerializers.class.getDeclaredMethod("writeS", MCDataOutput.class, short.class);

        Method deserializer = NBTData.class.getMethod("readString", NBTBase.class);
        Method serializer = NBTData.class.getMethod("writeString", String.class);

        compileSerializer(SerializationTarget.NBT, SerializationMethod.VALUE, serializer, deserializer);
    }

}
