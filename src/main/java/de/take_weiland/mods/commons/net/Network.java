package de.take_weiland.mods.commons.net;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.net.NettyBlob;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.util.Players;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author diesieben07
 */
public final class Network {

    public static final int DEFAULT_EXPECTED_SIZE = 32;

    private static final GenericFutureListener<?>[] EMPTY_LISTENERS = {};

    public static MCDataOutput newOutput() {
        return new MCDataOutputImpl(DEFAULT_EXPECTED_SIZE);
    }

    public static MCDataOutput newOutput(int expectedSize) {
        return new MCDataOutputImpl(expectedSize);
    }

    public static MCDataInput newInput(byte[] bytes) {
        return new MCDataInputImpl(bytes, 0, bytes.length);
    }

    public static void register(String channel, BiConsumer<? super byte[], ? super EntityPlayer> handler) {
        NetworkImpl.register(channel, handler);
    }

    public static void sendToServer(String channel, byte[] data) {
        SevenCommons.proxy.getClientNetworkManager()
                .scheduleOutboundPacket(new S3FPacketCustomPayload(channel, data), EMPTY_LISTENERS);
    }

    public static void sendToPlayer(EntityPlayer player, String channel, byte[] data) {
        sendToPlayer(player, new C17PacketCustomPayload(channel, data));
    }

    public static void sendToPlayers(Iterable<? extends EntityPlayer> players, String channel, byte[] data, Predicate<? super EntityPlayer> filter) {
        if (filter == null) {
            sendToPlayers(players, channel, data);
        } else {
            C17PacketCustomPayload packet = new C17PacketCustomPayload(channel, data);
            for (EntityPlayer player : players) {
                if (filter.test(player)) {
                    sendToPlayer(player, packet);
                }
            }
        }
    }

    public static void sendToPlayers(Iterable<? extends EntityPlayer> players, String channel, byte[] data) {
        C17PacketCustomPayload packet = new C17PacketCustomPayload(channel, data);

        for (EntityPlayer player : players) {
            sendToPlayer(player, packet);
        }
    }

    private static void sendToPlayer(EntityPlayer player, net.minecraft.network.Packet packet) {
        Players.checkNotClient(player).playerNetServerHandler.netManager.scheduleOutboundPacket(packet, EMPTY_LISTENERS);
    }

    public static <P> void sendToPlayer(EntityPlayer player, String channel, P packet, BiConsumer<? super P, ? super EntityPlayer> handler, BiFunction<? super P, ? super EntityPlayer, ? extends byte[]> encoder) {
        Players.checkNotClient(player).playerNetServerHandler.netManager.channel()
                .writeAndFlush(new NettyBlob<>(channel, packet, handler, encoder))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }


    public static SimpleChannelBuilder newSimpleChannel(String channel) {
        return new SimpleChannelBuilderImpl(channel);
    }

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    static <P extends Packet> Class<P> findPacketClassReflectively(PacketConstructor<P> constructor) {
        Class<?> myClazz = constructor.getClass();

        TypeToken<?> type = TypeToken.of(myClazz);
        Class<?> result;
        result = type.resolveType(function2ndParam).getRawType();
        if (!Packet.class.isAssignableFrom(result)) {
            result = Packet.class;
        }

        if (result == Packet.class) { // class is not a real subtype of Packet, so did not find an actual type parameter
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

                    if (Packet.class.isAssignableFrom(returnClass) && returnClass != Packet.class) {
                        result = returnClass;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (result == Packet.class) {
            throw new RuntimeException("Failed to reflectively find type argument of PacketConstructor. " +
                    "Please either refactor your code according to the docs or override getPacketClass.");
        }
        //noinspection unchecked
        return (Class<P>) result;
    }

}
