package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public final class PacketToChannelMap {

    private static Map<Class<? extends BaseModPacket>, SimplePacketData<?>> channels = new ConcurrentHashMap<>();

    public static <P extends Packet> SimplePacketData.Normal<P> getData(P packet) {
        //noinspection unchecked
        return (SimplePacketData.Normal<P>) getDataInternal(packet);
    }

    private static SimplePacketData<?> getDataInternal(BaseModPacket packet) {
        SimplePacketData<?> data = channels.get(packet.getClass());
        if (data == null) {
            throw new IllegalStateException(String.format("Cannot send unregistered Packet %s", packet.getClass().getName()));
        }
        return data;
    }

    public static <P extends Packet.WithResponse<R>, R extends Packet> SimplePacketData.WithResponse<P, R> getData(P packet) {
        //noinspection unchecked
        return (SimplePacketData.WithResponse<P, R>) getDataInternal(packet);
    }

    public static synchronized <P extends Packet> void register(Class<P> clazz, String channel, int id, BiFunction<? super MCDataInput, ? super EntityPlayer, ? extends P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
        channels.putIfAbsent(clazz, new SimplePacketData.Normal<>(channel, id, constructor, handler));
    }

    public static synchronized <P extends Packet.WithResponse<R>, R extends Packet> void register(Class<P> clazz, String channel, int id, BiFunction<? super MCDataInput, ? super EntityPlayer, ? extends P> constructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        channels.putIfAbsent(clazz, new SimplePacketData.WithResponse<>(channel, id, constructor, handler));
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
