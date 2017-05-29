package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.Scheduler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

/**
 *
 * <p>Core parts of the SevenCommons Network system, like sending and receiving packets.</p>
 *
 * @author diesieben07
 */
public final class NetworkImpl {

    public static final Logger LOGGER = SevenCommons.scLogger("Network");
    private static Map<String, ChannelHandler> channels = new ConcurrentHashMap<>();

    private static final String MULTIPART_CHANNEL = "SevenCommons|MP";

    private static final int MULTIPART_PREFIX = 0;
    private static final int MULTIPART_DATA = 1;
    // receiving

    public static void sendPacket(InternalPacket packet, EntityPlayerMP player) {
        sendPacket(packet, player.connection.netManager);
    }

    public static void sendPacket(InternalPacket packet, NetworkManager nm) {
        nm.channel().writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static Packet<INetHandlerPlayClient> toClientVanillaPacket(InternalPacket packet) {
        return new SPacketCustomPayload(packet._sc$internal$channel(), encodePacket(packet));
    }

    public static Packet<INetHandlerPlayServer> toServerVanillaPacket(InternalPacket packet) {
        return SevenCommons.proxy.newServerboundPacket(packet._sc$internal$channel(), encodePacket(packet));
    }

    private static PacketBuffer encodePacket(InternalPacket packet) {
        MCDataOutput out = Network.newOutput(packet._sc$internal$expectedSize());
        try {
            packet._sc$internal$writeTo(out);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Packet %s threw an exception during writing", packet), e);
        }
        return new PacketBuffer(out.asByteBuf());
    }

    public static synchronized void register(String channel, ChannelHandler handler) {
        checkNotFrozen();
        if (channels.putIfAbsent(channel, handler) != null) {
            throw new IllegalStateException(String.format("Channel %s already registered", channel));
        }
    }

    private static synchronized void freeze() {
        channels = ImmutableMap.copyOf(channels);
    }

    private static void checkNotFrozen() {
        checkState(!(channels instanceof ImmutableMap), "Must register packets before postInit");
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, NetworkImpl::freeze);
    }

    public static void handleServersideConnection(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        NetHandlerPlayServer handler = (NetHandlerPlayServer) event.getHandler();
        ChannelPipeline pipeline = handler.netManager.channel().pipeline();

        if (!event.isLocal()) {
            insertEncoder(pipeline, SCToServerMessageEncoder.INSTANCE);
        }

        insertHandler(pipeline, new SCMessageHandlerServer(handler.getNetworkManager()));
    }

    @SideOnly(Side.CLIENT)
    public static void handleClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        NetHandlerPlayClient handler = (NetHandlerPlayClient) event.getHandler();
        ChannelPipeline pipeline = handler.getNetworkManager().channel().pipeline();

        if (!event.isLocal()) {
            // only need the encoder when not connected locally
            insertEncoder(pipeline, SCToServerMessageEncoder.INSTANCE);
        }
        // handler handles both direct messages (for local) and the vanilla-payload packet
        insertHandler(pipeline, SCMessageHandlerClient.INSTANCE);
    }

    private static void insertHandler(ChannelPipeline pipeline, io.netty.channel.ChannelHandler handler) {
        pipeline.addBefore("packet_handler", "sevencommons:handler", handler);
    }

    private static void insertEncoder(ChannelPipeline pipeline, io.netty.channel.ChannelHandler encoder) {
        // this is "backwards" - outbound messages travel "upwards" in the pipeline
        // so really the order is sevencommons:encoder and then vanilla's encoder
        pipeline.addAfter("encoder", "sevencommons:encoder", encoder);
    }

    private NetworkImpl() {
    }

    static boolean handleCustomPayload(String channel, PacketBuffer payload, byte side, NetworkManager manager) {
        ChannelHandler handler = channels.get(channel);
        if (handler == null) {
            return false;
        } else {
            handler.accept(channel, payload, side, manager);
            return true;
        }
    }

    // utilities

    public static EntityPlayer getPlayer(byte side, NetworkManager manager) {
        return side == Network.CLIENT
                ? SevenCommons.proxy.getClientPlayer()
                : ((NetHandlerPlayServer) manager.getNetHandler()).player;
    }

    public static Scheduler getScheduler(byte side) {
        return side == Network.CLIENT ? Scheduler.Companion.getClient() : Scheduler.server;
    }

    public static void validateSide(byte characteristics, byte side, Object packet) {
        if ((characteristics & side) == 0) {
            throw new ProtocolException("Packet " + packet + " received on wrong side " + (side == Network.CLIENT ? "client" : "server"));
        }
    }

    private static final java.lang.reflect.Type function2ndParam = Function.class.getTypeParameters()[1];

    public static <P extends PacketBase> Class<P> findPacketClassReflectively(PacketConstructor<P> constructor) {
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

    public static Optional<Side> findReceivingSideReflectively(PacketHandlerBase handler) {
        PacketHandler.ReceivingSide annotation = findAnnotatedElement(handler).getAnnotation(PacketHandler.ReceivingSide.class);
        return annotation == null ? Optional.empty() : Optional.of(annotation.value());
    }

    public static boolean findIsAsyncReflectively(PacketHandlerBase handler) {
        return findAnnotatedElement(handler).getAnnotation(PacketHandler.Async.class) != null;
    }

    private static AnnotatedElement findAnnotatedElement(Object functionalInterfaceInstance) {
        Class<?> clazz = functionalInterfaceInstance.getClass();
        try {
            Method writeReplace = clazz.getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            Object serForm = writeReplace.invoke(functionalInterfaceInstance);
            if (serForm instanceof SerializedLambda) {
                SerializedLambda serLambda = (SerializedLambda) serForm;

                Class<?> clazzHoldingLambda = Class.forName(serLambda.getImplClass());

                switch (serLambda.getImplMethodKind()) {
                    case MethodHandleInfo.REF_newInvokeSpecial:
                        for (Constructor<?> constructor : clazzHoldingLambda.getDeclaredConstructors()) {
                            if (Type.getConstructorDescriptor(constructor).equals(serLambda.getImplMethodSignature())) {
                                return constructor;
                            }
                        }
                        break;
                    case MethodHandleInfo.REF_invokeInterface:
                    case MethodHandleInfo.REF_invokeSpecial:
                    case MethodHandleInfo.REF_invokeStatic:
                    case MethodHandleInfo.REF_invokeVirtual:
                        for (Method method : clazzHoldingLambda.getDeclaredMethods()) {
                            if (method.getName().equals(serLambda.getImplMethodName()) && Type.getMethodDescriptor(method).equals(serLambda.getImplMethodSignature())) {
                                return method;
                            }
                        }
                        break;
                }
            }
        } catch (Exception ignored) {
        }
        return clazz;
    }
}
