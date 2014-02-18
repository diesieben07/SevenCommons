package de.take_weiland.mods.commons.fastreflect;

import com.google.common.base.Preconditions;
import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class Fastreflect {

	public static <T> T createAccessor(Class<T> iface) {
		return strategy.createAccessor(Preconditions.checkNotNull(iface));
	}
	
	public static Class<?> defineDynamicClass(byte[] clazz) {
		return defineDynamicClass(clazz, Fastreflect.class);
	}
	
	public static Class<?> defineDynamicClass(byte[] clazz, Class<?> context) {
		return strategy.defineDynClass(clazz, context);
	}
	
	public static String nextDynamicClassName() {
		return "de/take_weiland/mods/commons/fastreflect/dyn/Dyn" + nextId.getAndIncrement();
	}
	
	private static final FastreflectStrategy strategy;
	private static final Logger logger;
	
	static {
		strategy = selectStrategy();
		FMLLog.makeLog("SC|Fastreflect");
		logger = Logger.getLogger("SC|Fastreflect");
	}

	private static FastreflectStrategy selectStrategy() {
		if (JavaUtils.hasUnsafe()) {
			try {
				return Class.forName("de.take_weiland.mods.commons.fastreflect.SunProprietaryStrategy").asSubclass(FastreflectStrategy.class).newInstance();
			} catch (Exception e) {
				// then not
			}
		}
		
		logger.warning("Using slow Strategy! This may lead to performance penalties. Please use Oracle's VM.");
		
		return new ReflectiveStrategy();
	}

	static final AtomicInteger nextId = new AtomicInteger(0);

}
