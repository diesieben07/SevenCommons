package de.take_weiland.mods.commons.internal.exclude;

import com.google.common.collect.ImmutableSet;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author diesieben07
 */
public final class ClassInfoUtil {

    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(new ClassInfoUtil());
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

    private static Map<String, ImmutableSet<String>> superCache = new ConcurrentHashMap<>();

    public static Set<String> getSupers(ClassInfo classInfo) {
        // grab a local var in case of concurrency
        Map<String, ImmutableSet<String>> superCacheLocal = superCache;
        if (superCacheLocal != null) {
            return superCacheLocal.computeIfAbsent(classInfo.internalName(), cl -> buildSupers(classInfo));
        } else {
            return buildSupers(classInfo);
        }
    }

    private static Set<String> getSupers(String clazz) {
        Map<String, ImmutableSet<String>> superCacheLocal = superCache;
        if (superCacheLocal != null) {
            return superCacheLocal.computeIfAbsent(clazz, ClassInfoUtil::buildSupers);
        } else {
            return buildSupers(clazz);
        }
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

    private ClassInfoUtil() {
    }

}
