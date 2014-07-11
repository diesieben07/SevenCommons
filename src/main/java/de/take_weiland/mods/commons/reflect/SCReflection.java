package de.take_weiland.mods.commons.reflect;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>Faster alternative to traditional reflection. Works with so called "Accessor Interfaces" which define getters, setters or delegate methods, which just invoke the target method.
 * See {@link de.take_weiland.mods.commons.reflect.Getter @Getter}, {@link de.take_weiland.mods.commons.reflect.Setter @Setter} and {@link de.take_weiland.mods.commons.reflect.Invoke @Inovoke}
 * for further explanation.</p>
 * <p>This class uses proprietary APIs when possible to achieve no-cost reflection (except the method call to the accessor interface). If these APIs are not present,
 * traditional Reflection with a {@link java.lang.reflect.Proxy} is used.</p>
 */
public final class SCReflection {

	private static final boolean DEBUG = true;

	/**
	 * <p>create an Instance of the given Accessor Interface. The result of this method should be permanently cached, because
	 * this method defines a new class every time it is invoked.</p>
	 *
	 * @param iface the Accessor Interface
	 * @return a newly created object, implementing the given interface
	 */
	public static <T> T createAccessor(Class<T> iface) {
		return strategy.createAccessor(Preconditions.checkNotNull(iface));
	}

	/**
	 * define a temporary class from the bytes which can be garbage collected if no longer in use.
	 *
	 * @param clazz the bytes describing the class
	 * @return the defined class
	 */
	public static Class<?> defineDynamicClass(byte[] clazz) {
		Class<?> def = defineDynamicClass(clazz, SCReflection.class);
		if (DEBUG) {
			try {
				File file = new File("sevencommonsdyn/" + def.getName().replace('.', '/') + ".class");
				Files.createParentDirs(file);
				OutputStream out = new FileOutputStream(file);
				out.write(clazz);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return def;
	}

	/**
	 * Same as {@link #defineDynamicClass(byte[])} but defines the class in the given context
	 */
	public static Class<?> defineDynamicClass(byte[] clazz, Class<?> context) {
		return strategy.defineDynClass(clazz, context);
	}

	/**
	 * get a unique name for a dynamic class
	 *
	 * @return a unique name
	 */
	public static String nextDynamicClassName() {
		return nextDynamicClassName("de/take_weiland/mods/commons/reflect/dyn");
	}

	public static String nextDynamicClassName(Package pkg) {
		return nextDynamicClassName(pkg.getName());
	}

	public static String nextDynamicClassName(String pkg) {
		return ASMUtils.internalName(pkg) + "/_sc_dyn_" + nextId.getAndIncrement();
	}

	public static Class<?> getCallerClass() {
		// see comments in getCallerClass(int level)
		return sm.getClassStack()[3];
	}

	public static Class<?> getCallerClass(int level) {
		checkArgument(level >= 0);
		Class<?>[] stack = sm.getClassStack();
		// 0 is the security manager
		// 1 is us (Fastreflect.getCallerClass)
		// 2 is our caller
		// 3 is the caller of our caller (equivalent to level 0)
		return JavaUtils.get(stack, level + 3);
	}

	private static final AtomicInteger nextId = new AtomicInteger(0);
	private static final FastreflectSecurityManager sm = new FastreflectSecurityManager();
	private static final ReflectionStrategy strategy = selectStrategy();
	private static final Logger logger = null; //SevenCommons.scLogger();

	static {
	}

	private static ReflectionStrategy selectStrategy() {
		if (JavaUtils.hasUnsafe()) {
			try {
				return Class.forName("de.take_weiland.mods.commons.reflect.SunProprietaryStrategy").asSubclass(ReflectionStrategy.class).newInstance();
			} catch (Exception e) {
				// then not
			}
		}

		logger.warning("Using slow Strategy! This may lead to performance penalties. Please use Oracle's VM.");

		return new NativeJavaStrategy();
	}

	private static class FastreflectSecurityManager extends SecurityManager {

		Class<?>[] getClassStack() {
			return getClassContext();
		}
	}

}
