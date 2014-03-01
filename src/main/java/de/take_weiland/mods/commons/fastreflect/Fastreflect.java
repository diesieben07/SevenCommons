package de.take_weiland.mods.commons.fastreflect;

import com.google.common.base.Preconditions;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.MiscUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * <p>Faster alternative to traditional reflection. Works with so called "Accessor Interfaces" which define getters, setters or delegate methods, which just invoke the target method.
 * See {@link de.take_weiland.mods.commons.fastreflect.Getter @Getter}, {@link de.take_weiland.mods.commons.fastreflect.Setter @Setter} and {@link de.take_weiland.mods.commons.fastreflect.Invoke @Inovoke}
 * for further explanation.</p>
 * <p>This class uses proprietary APIs when possible to achieve no-cost reflection (except the method call to the accessor interface). If these APIs are not present,
 * traditional Reflection with a {@link java.lang.reflect.Proxy} is used.</p>
 */
public final class Fastreflect {

	/**
	 * <p>create an Instance of the given Accessor Interface. The result of this method should be permanently cached, because
	 * this method defines a new class every time it is invoked.</p>
	 * @param iface the Accessor Interface
	 * @return a newly created object, implementing the given interface
	 */
	public static <T> T createAccessor(Class<T> iface) {
		return strategy.createAccessor(Preconditions.checkNotNull(iface));
	}

	/**
	 * define a temporary class from the bytes which can be garbage collected if no longer in use.
	 * @param clazz the bytes describing the class
	 * @return the defined class
	 */
	public static Class<?> defineDynamicClass(byte[] clazz) {
		return defineDynamicClass(clazz, Fastreflect.class);
	}

	/**
	 * Same as {@link #defineDynamicClass(byte[])} but defines the class in the given context
	 */
	public static Class<?> defineDynamicClass(byte[] clazz, Class<?> context) {
		return strategy.defineDynClass(clazz, context);
	}

	/**
	 * get a unique name for a dynamic class
	 * @return a unique name
	 */
	public static String nextDynamicClassName() {
		return "de/take_weiland/mods/commons/fastreflect/dyn/Dyn" + nextId.getAndIncrement();
	}
	
	private static final FastreflectStrategy strategy;
	private static final Logger logger;
	
	static {
		strategy = selectStrategy();
		logger = MiscUtil.getLogger("SC|Fastreflect");
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
