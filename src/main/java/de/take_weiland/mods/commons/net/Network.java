package de.take_weiland.mods.commons.net;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.util.Players;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.entity.player.EntityPlayer;
import org.objectweb.asm.Type;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Network {

    public static final int DEFAULT_EXPECTED_SIZE = 32;

    public static MCDataOutput newOutput() {
        return new MCDataOutputImpl(DEFAULT_EXPECTED_SIZE);
    }

    public static MCDataOutput newOutput(int expectedSize) {
        return new MCDataOutputImpl(expectedSize);
    }

    public static MCDataInput newInput(byte[] bytes) {
        return new MCDataInputImpl(bytes, 0, bytes.length);
    }

    public static MCDataInput newInput(byte[] bytes, int off, int len) {
        return new MCDataInputImpl(bytes, off, len);
    }

    public static void registerHandler(String channel, BiConsumer<? super byte[], ? super EntityPlayer> handler) {
        NetworkImpl.register(channel, handler);
    }

    public static void sendToPlayer(EntityPlayer player, RawPacket packet) {
        Players.checkNotClient(player).playerNetServerHandler.netManager.channel()
                .writeAndFlush(packet)
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToServer(RawPacket packet) {
        SevenCommons.proxy.getClientNetworkManager().channel()
                .writeAndFlush(packet)
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static SimpleChannelBuilder newSimpleChannel(String channel) {
        return new SimpleChannelBuilderImpl(channel);
    }

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    static <P extends BasePacket> Class<P> findPacketClassReflectively(PacketConstructor<P> constructor) {
        Class<?> myClazz = constructor.getClass();

        TypeToken<?> type = TypeToken.of(myClazz);
        Class<?> result;
        result = type.resolveType(function2ndParam).getRawType();
        if (!BasePacket.class.isAssignableFrom(result)) {
            result = BasePacket.class;
        }

        if (result == BasePacket.class) { // class is not a real subtype of Packet, so did not find an actual type parameter
            // try lambda-hackery now
            try {
                Method method = myClazz.getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                Object serForm = method.invoke(constructor);
                if (serForm instanceof SerializedLambda) {
                    SerializedLambda serLambda = (SerializedLambda) serForm;

                    Class<?> returnClass = Packet.class;
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

                    if (BasePacket.class.isAssignableFrom(returnClass) && returnClass != BasePacket.class) {
                        result = returnClass;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (result == BasePacket.class) {
            throw new RuntimeException("Failed to reflectively find type argument of PacketConstructor. " +
                    "Please either refactor your code according to the docs or override getPacketClass.");
        }
        //noinspection unchecked
        return (Class<P>) result;
    }

}
