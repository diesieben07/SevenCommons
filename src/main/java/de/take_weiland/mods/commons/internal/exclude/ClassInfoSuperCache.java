package de.take_weiland.mods.commons.internal.exclude;

import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public final class ClassInfoSuperCache {

    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(new ClassInfoSuperCache());
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.world.isRemote) {
            return;
        }
        // null out the superCache once any world is loaded
        // ClassInfo is only really used with ASM code (class transformers)
        // they should be done at this point and holding onto all this information
        // is nothing but a memory hog
        superCache = null;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    private static volatile Map<String, ImmutableSet<String>> superCache = new ConcurrentHashMap<>();

    public static Set<String> getSupers(ClassInfo classInfo) {
        return computeIfAbsentFixed(superCache, classInfo.internalName(), s -> buildSupers(classInfo));
    }

    private static Set<String> getSupers(String clazz) {
        return computeIfAbsentFixed(superCache, clazz, ClassInfoSuperCache::buildSupers);
    }

    private static ImmutableSet<String> buildSupers(String clazz) {
        return buildSupers(ClassInfo.of(clazz));
    }

    private static ImmutableSet<String> buildSupers(ClassInfo classInfo) {
        return buildSupers(classInfo.superName(), classInfo.interfaces());
    }

    private static ImmutableSet<String> buildSupers(String superName, Iterable<String> interfaces) {
        Set<String> set = new HashSet<>();
        if (superName != null) {
            set.add(superName);
            set.addAll(getSupers(superName));
        }

        for (String iface : interfaces) {
            if (set.add(iface)) {
                set.addAll(getSupers(iface));
            }
        }
        return ImmutableSet.copyOf(set);
    }

    // cannot use computeIfAbsent because it is atomic in ConcurrentHashMap
    // and doesn't allow "chained" computations
    // which does happen with inheritance
    private static <K, V> V computeIfAbsentFixed(@Nullable Map<K, V> map, K key, Function<? super K, ? extends V> computation) {
        if (map == null) {
            return computation.apply(key);
        }
        V value = map.get(key);
        if (value == null) {
            value = computation.apply(key);
            V valueRaced = map.putIfAbsent(key, value);
            return valueRaced == null ? value : valueRaced;
        } else {
            return value;
        }
    }

    private ClassInfoSuperCache() {
    }

}
