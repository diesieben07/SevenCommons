package de.take_weiland.mods.commons.net;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.net.BaseModPacket;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.objectweb.asm.Type;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
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

    public static void registerHandler(String channel, RawPacketHandler handler) {
        NetworkImpl.register(channel, handler);
    }

    public static void sendToServer(RawPacket packet) {
        NetworkImpl.sendToServer((BaseNettyPacket) packet);
    }

    public static void sendTo(RawPacket packet, EntityPlayerMP player) {
        NetworkImpl.sendToPlayer(player, (BaseNettyPacket) packet);
    }

    public static void sendTo(RawPacket packet, Iterable<? extends EntityPlayer> players) {
        NetworkImpl.sendTo(players, (BaseNettyPacket) packet);
    }

    public static SimpleChannelBuilder newSimpleChannel(String channel) {
        return new SimpleChannelBuilderImpl(channel);
    }

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    static <P extends SimpleModPacketBase> Class<P> findPacketClassReflectively(PacketConstructor<P> constructor) {
        Class<?> myClazz = constructor.getClass();

        TypeToken<?> type = TypeToken.of(myClazz);
        Class<?> result;
        result = type.resolveType(function2ndParam).getRawType();
        if (!SimpleModPacketBase.class.isAssignableFrom(result)) {
            result = BaseModPacket.class;
        }

        if (result == SimpleModPacketBase.class) { // class is not a real subtype of Packet, so did not find an actual type parameter
            // try lambda-hackery now
            try {
                Method method = myClazz.getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                Object serForm = method.invoke(constructor);
                if (serForm instanceof SerializedLambda) {
                    SerializedLambda serLambda = (SerializedLambda) serForm;

                    Class<?> returnClass = SimpleModPacketBase.class;
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

                    if (SimpleModPacketBase.class.isAssignableFrom(returnClass) && returnClass != SimpleModPacketBase.class) {
                        result = returnClass;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (result == SimpleModPacketBase.class) {
            throw new RuntimeException("Failed to reflectively find type argument of PacketConstructor. " +
                    "Please either refactor your code according to the docs or override getPacketClass.");
        }
        //noinspection unchecked
        return (Class<P>) result;
    }

}
