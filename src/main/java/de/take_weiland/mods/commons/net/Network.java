package de.take_weiland.mods.commons.net;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import org.objectweb.asm.Type;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * <p>Central registry and factory class for networking.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Network {

    /**
     * <p>Default buffer size.</p>
     */
    public static final int DEFAULT_BUFFER_SIZE = 32;
    /**
     * <p>Specifies that a packet or handler is designed to operate only on the client side.</p>
     */
    public static final byte CLIENT = 0b0001;
    /**
     * <p>Specifies that a packet or handler is designed to operate only on the server side.</p>
     */
    public static final byte SERVER = 0b0010;
    /**
     * <p>Specifies that a packet or handler is designed to operate on both client and server side. Equivalent to {@code CLIENT | SERVER}.</p>
     */
    public static final byte BIDIRECTIONAL = CLIENT | SERVER;
    /**
     * <p>Specifies that a packet or handler may be handled on some other than the main game thread for the
     * receiving side. Which thread is used is not specified and up to the implementation.</p>
     */
    public static final byte ASYNC = 0b0100;

    /**
     * <p>Create a new {@code MCDataOutput} with the default buffer size.</p>
     *
     * @return a new {@code MCDataOutput}.
     */
    public static MCDataOutput newOutput() {
        return new MCDataOutputImpl(DEFAULT_BUFFER_SIZE);
    }

    /**
     * <p>Create a new {@code MCDataOutput} with the given buffer size.</p>
     *
     * @return a new {@code MCDataOutput}.
     */
    public static MCDataOutput newOutput(int expectedSize) {
        return new MCDataOutputImpl(expectedSize);
    }

    /**
     * <p>Create a new {@code MCDataInput} that reads from the given array.</p>
     *
     * @return a new {@code MCDataInput}.
     */
    public static MCDataInput newInput(byte[] bytes) {
        return new MCDataInputImpl(bytes, 0, bytes.length);
    }

    /**
     * <p>Create a new {@code MCDataInput} that reads at most {@code len} bytes from the given array starting at
     * position {@code off}.</p>
     *
     * @return a new {@code MCDataInput}.
     */
    public static MCDataInput newInput(byte[] bytes, int off, int len) {
        return new MCDataInputImpl(bytes, off, len);
    }

    /**
     * <p>Register a new {@code ChannelHandler} for the given channel.</p>
     *
     * @param channel the channel
     * @param handler the handlerSS
     */
    public static void registerHandler(String channel, ChannelHandler handler) {
        NetworkImpl.register(channel, handler);
    }

    public static SimpleChannelBuilder newSimpleChannel(String channel) {
        return new SimpleChannelBuilderImpl(channel);
    }

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    static <P extends PacketBase> Class<P> findPacketClassReflectively(PacketConstructor<P> constructor) {
        Class<?> myClazz = constructor.getClass();

        TypeToken<?> type = TypeToken.of(myClazz);
        Class<?> result;
        result = type.resolveType(function2ndParam).getRawType();
        if (!PacketBase.class.isAssignableFrom(result)) {
            result = PacketBase.class;
        }

        if (result == PacketBase.class) { // class is not a real subtype of Packet, so did not find an actual type parameter
            // try lambda-hackery now
            try {
                Method method = myClazz.getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                Object serForm = method.invoke(constructor);
                if (serForm instanceof SerializedLambda) {
                    SerializedLambda serLambda = (SerializedLambda) serForm;

                    Class<?> returnClass = PacketBase.class;
                    switch (serLambda.getImplMethodKind()) {
                        case MethodHandleInfo.REF_newInvokeSpecial:
                            returnClass = Class.forName(Type.getObjectType(serLambda.getImplClass()).getClassName());
                            break;
                        case MethodHandleInfo.REF_invokeInterface:
                        case MethodHandleInfo.REF_invokeSpecial:
                        case MethodHandleInfo.REF_invokeStatic:
                        case MethodHandleInfo.REF_invokeVirtual:
                            returnClass = Class.forName(Type.getReturnType(serLambda.getImplMethodSignature()).getClassName());
                            break;
                    }

                    if (PacketBase.class.isAssignableFrom(returnClass) && returnClass != PacketBase.class) {
                        result = returnClass;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (result == PacketBase.class) {
            throw new RuntimeException("Failed to reflectively find type argument of PacketConstructor. " +
                    "Please either refactor your code according to the docs or override getPacketClass.");
        }
        //noinspection unchecked
        return (Class<P>) result;
    }

}
