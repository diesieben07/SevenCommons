package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public final class PacketToChannelMap {

    public static final String PACKET_DATA_FIELD = "_sc$pkt$data";

    private static Map<Class<?>, SimplePacketData> channels = new ConcurrentHashMap<>();

    public static <P extends Packet.WithResponse<R>, R extends Packet.Response> SimplePacketData.WithResponse<P, R> getData(P packet) {
        //noinspection unchecked
        return (SimplePacketData.WithResponse<P, R>) ((BaseModPacket) packet)._sc$getData();
    }

    public static SimplePacketData getDataFallback(BaseModPacket packet) {
        SimplePacketData data = channels.get(packet.getClass());
        if (data == null) {
            throw new IllegalStateException(String.format("Cannot send unregistered Packet %s", packet.getClass().getName()));
        }
        return data;
    }

    public static synchronized <P extends Packet> void register(Class<P> clazz, String channel, int id, byte info, BiConsumer<? super P, ? super EntityPlayer> handler) {
        register0(clazz, new SimplePacketData.Normal<>(channel, id, info, handler));
    }

    public static synchronized <P extends Packet.WithResponse<R>, R extends Packet.Response> void register(Class<P> clazz, String channel, int id, byte info, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        register0(clazz, new SimplePacketData.WithResponseNormal<>(channel, id, info, handler));
    }

    public static synchronized <P extends Packet.WithResponse<R>, R extends Packet.Response> void registerFuture(Class<P> clazz, String channel, int id, byte info, BiFunction<? super P, ? super EntityPlayer, ? extends CompletionStage<? extends R>> handler) {
        register0(clazz, new SimplePacketData.WithResponseFuture<>(channel, id, info, handler));
    }

    private static void register0(Class<?> clazz, SimplePacketData data) {
        if (channels.putIfAbsent(clazz, data) != null) {
            throw new IllegalArgumentException(String.format("Packet class %s used twice", clazz.getName()));
        }
        try {
            // the field is static final, so use unsafe to set it
            Field field = clazz.getDeclaredField(PACKET_DATA_FIELD);
            Unsafe unsafe = JavaUtils.unsafe();
            Object base = unsafe.staticFieldBase(field);
            long fieldOffset = unsafe.staticFieldOffset(field);
            unsafe.putObject(base, fieldOffset, data);
        } catch (NoSuchFieldException e) {
            // ignored
        }
    }

    private static synchronized void freeze() {
        channels = ImmutableMap.copyOf(channels);
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, PacketToChannelMap::freeze);
    }

    private PacketToChannelMap() {
    }

}
