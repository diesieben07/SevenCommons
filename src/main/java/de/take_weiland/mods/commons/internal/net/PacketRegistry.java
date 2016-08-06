package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraftforge.fml.common.LoaderState;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps packet class => PacketData
 * @author diesieben07
 */
public final class PacketRegistry {

    private static Map<Class<?>, PacketData> map = new HashMap<>();

    public static final ClassValue<PacketData> classValue = new ClassValue<PacketData>() {
        @Override
        protected PacketData computeValue(Class<?> type) {
            return getData0(type);
        }
    };

    static synchronized PacketData getData0(Class<?> clazz) {
        return map.get(clazz);
    }

    public static synchronized void register(Class<? extends PacketWithData> clazz, PacketData data) {
        if (map instanceof ImmutableMap) {
            throw new IllegalStateException("Packets must be registered before postInit");
        }
        if (map.putIfAbsent(clazz, data) != null) {
            throw new IllegalArgumentException("Packet class " + clazz + " used twice.");
        }
        classValue.remove(clazz);
    }

    private static synchronized void freeze() {
        map = ImmutableMap.copyOf(map);
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, PacketRegistry::freeze);
    }

}
