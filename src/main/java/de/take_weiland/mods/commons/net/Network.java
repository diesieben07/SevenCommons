package de.take_weiland.mods.commons.net;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.net.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.relauncher.Side;
import org.objectweb.asm.Type;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
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
        return newOutput(DEFAULT_BUFFER_SIZE);
    }

    /**
     * <p>Create a new {@code MCDataOutput} with the given buffer size.</p>
     *
     * @return a new {@code MCDataOutput}.
     */
    public static MCDataOutput newOutput(int expectedSize) {
        return new MCDataOutputImpl(Unpooled.buffer(expectedSize));
    }

    /**
     * <p>Create a new {@code MCDataInput} that reads from the given buffer.</p>
     *
     * @return a new {@code MCDataInput}.
     */
    public static MCDataInput newInput(ByteBuf buf) {
        return new MCDataInputImpl(buf);
    }

    public static SimpleChannelBuilder newSimpleChannel(String channel) {
        return new SimpleChannelBuilderImpl(channel);
    }

}
