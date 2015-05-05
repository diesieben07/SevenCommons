package de.take_weiland.mods.commons.net;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.PacketCodecPair;
import de.take_weiland.mods.commons.util.Players;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author diesieben07
 */
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

    public static <P> void newChannel(PacketCodec<P> codec) {
        NetworkImpl.register(codec.channel(), codec);
    }

    public static <P> PacketCodec<P> newChannel(String channel,
                                                Function<? super P, ? extends byte[]> encoder,
                                                Function<? super byte[], ? extends P> decoder,
                                                BiConsumer<? super P, ? super EntityPlayer> handler) {
        return NetworkImpl.register(channel, new PacketCodec<P>() {
            @Override
            public byte[] encode(P packet) {
                return encoder.apply(packet);
            }

            @Override
            public P decode(byte[] payload) {
                return decoder.apply(payload);
            }

            @Override
            public void handle(P packet, EntityPlayer player) {
                handler.accept(packet, player);
            }

            @Override
            public String channel() {
                return channel;
            }
        });
    }

    public static SimpleChannelBuilder newSimpleChannel(String channel) {
        return new SimpleChannelBuilderImpl(channel);
    }

    public static <P> void sendToServer(P packet, PacketCodec<P> codec) {
        send0(new PacketCodecPair<>(packet, codec), SevenCommons.proxy.getClientNetworkManager());
    }

    public static <P> void sendTo(PacketCodec<P> codec, P packet, EntityPlayer player) {
        sendToPlayer0(new PacketCodecPair<>(packet, codec), Players.checkNotClient(player));
    }

    public static <P> void sendTo(PacketCodec<P> codec, P packet, Iterable<? extends EntityPlayer> players) {
        sendTo(codec, packet, players, null);
    }

    public static <P> void sendTo(PacketCodec<P> codec, P packet, Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        Iterator<? extends EntityPlayer> it = players.iterator();
        PacketCodecPair<P> pair = new PacketCodecPair<>(packet, codec);

        while (it.hasNext()) {
            EntityPlayerMP player = Players.checkNotClient(it.next());
            if (filter == null || filter.test(player)) {
                sendToPlayer0(pair, player);
            }
        }
    }

    private static <P> void sendToPlayer0(PacketCodecPair<P> pair, EntityPlayerMP player) {
        send0(pair, player.playerNetServerHandler.netManager);
    }

    private static <P> void send0(PacketCodecPair<P> pair, NetworkManager manager) {
        manager.channel()
            .writeAndFlush(pair)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    static <P extends Packet> Class<P>    findPacketClassReflectively(PacketConstructor<P> constructor) {
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
            } catch (Exception ignored) { }
        }
        if (result == Packet.class) {
            throw new RuntimeException("Failed to reflectively find type argument of PacketConstructor. " +
                    "Please either refactor your code according to the docs or override getPacketClass.");
        }
        //noinspection unchecked
        return (Class<P>) result;
    }

}
