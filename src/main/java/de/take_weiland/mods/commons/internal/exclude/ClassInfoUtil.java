package de.take_weiland.mods.commons.internal.exclude;

import com.google.common.collect.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Map;
import java.util.Set;

/**
 * @author diesieben07
 */
public final class ClassInfoUtil {

	public static void preInit() {
		MinecraftForge.EVENT_BUS.register(new ClassInfoUtil());
	}

	@ForgeSubscribe
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

	private static volatile Map<String, ImmutableSet<String>> superCache = Maps.newHashMap();

	public static Set<String> getSupers(ClassInfo classInfo) {
		// grab a local var in case of concurrency
		Map<String, ImmutableSet<String>> superCacheLocal = superCache;
		if (superCacheLocal != null) {
			ImmutableSet<String> supers;
			synchronized (ClassInfoUtil.class) {
				supers = superCacheLocal.get(classInfo.internalName());
			}
			if (supers == null) {
				supers = buildSupers(classInfo);
				synchronized (ClassInfoUtil.class) {
					// in theory someone else might have done this already
					// but oh well, the data is the same
					superCacheLocal.put(classInfo.internalName(), supers);
				}
			}
			return supers;
		} else {
			return buildSupers(classInfo);
		}
	}

	private static ImmutableSet<String> buildSupers(ClassInfo classInfo) {
		Set<String> set = Sets.newHashSet();
		String superName = classInfo.superName();
		if (superName != null) {
			set.add(superName);
			set.addAll(classInfo.superclass().getSupers());
		}
		for (String iface : classInfo.interfaces()) {
			if (set.add(iface)) {
				set.addAll(ClassInfo.of(iface).getSupers());
			}
		}
		// use immutable set to reduce memory footprint and potentially increase performance
		// cannot use builder because we need the boolean return from set.add
		return ImmutableSet.copyOf(set);
	}

	private ClassInfoUtil() {
	}

}
