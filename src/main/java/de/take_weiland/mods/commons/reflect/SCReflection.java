package de.take_weiland.mods.commons.reflect;

import com.google.common.io.Files;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.internal.SevenCommonsLoader;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * <p>Reflection utilities.</p>
 * <p>This class provides an alternative to using traditional reflection to access inaccessible fields and methods.</p>
 */
@ParametersAreNonnullByDefault
public final class SCReflection {

	private SCReflection() { }

	private static final boolean DEBUG;

	static {
		DEBUG = Boolean.getBoolean("sevencommons.reflect.debug");
	}

	/**
	 * <p>Create an object that implements the given Accessor Interface. The result of this method should be permanently cached, because
	 * this method defines a new class every time it is invoked.</p>
	 * <p>The given class must be an interface. All methods must be marked with one of {@link de.take_weiland.mods.commons.reflect.Getter},
	 * {@link de.take_weiland.mods.commons.reflect.Setter}, {@link de.take_weiland.mods.commons.reflect.Invoke} or {@link de.take_weiland.mods.commons.reflect.Construct}
	 * and comply with the respective contract.</p>
	 *
	 * @param iface the Accessor Interface
	 * @return a newly created object, implementing the given interface
	 */
	public static <T> T createAccessor(Class<T> iface) {
		return strategy.createAccessor(iface);
	}

	/**
	 * <p>Define a new Class specified by the given bytes. If the class is no longer in use it will usually be
	 * garbage collected.</p>
	 *
	 * @param clazz the bytes describing the class
	 * @return the defined class
	 */
	public static Class<?> defineDynamicClass(byte[] clazz) {
		if (DEBUG) {
			try {
				ClassNode node = ASMUtils.getThinClassNode(clazz);
				File file = new File("sevencommonsdyn/" + node.name + ".class");
				Files.createParentDirs(file);
				OutputStream out = new FileOutputStream(file);
				out.write(clazz);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return defineDynamicClass(clazz, SCReflection.class);
	}

	/**
	 * <p>Define a new Class specified by the given bytes. If the class is no longer in use it will usually be
	 * garbage collected.</p>
	 *
	 * @param clazz the bytes describing the class
	 * @param context the class which to use as the context
	 * @return the defined class
	 */
	public static Class<?> defineDynamicClass(byte[] clazz, Class<?> context) {
		return strategy.defineDynClass(clazz, context);
	}

	/**
	 * <p>Get a new unique class name as an internal name.</p>
	 *
	 * @return a unique name
	 */
	public static String nextDynamicClassName() {
		return nextDynamicClassName("de/take_weiland/mods/commons/reflect/dyn");
	}

	/**
	 * <p>Get a new unique class name in the given package as an internal name.</p>
	 *
	 * @param pkg the package
	 * @return a unique name
	 */
	public static String nextDynamicClassName(Package pkg) {
		return nextDynamicClassName(pkg.getName());
	}

	/**
	 * <p>Get a new unique class name in the given package as an internal name.</p>
	 *
	 * @param pkg the package
	 * @return a unique name
	 */
	public static String nextDynamicClassName(String pkg) {
		return ASMUtils.internalName(pkg) + "/_sc_dyn_" + nextId.getAndIncrement();
	}

	private static final Logger logger = SevenCommonsLoader.scLogger("Reflection");
	private static final AtomicInteger nextId = new AtomicInteger(0);
	private static final ReflectionStrategy strategy = selectStrategy();

	private static ReflectionStrategy selectStrategy() {
		if (JavaUtils.hasUnsafe()) {
			try {
				return (ReflectionStrategy) Class.forName("de.take_weiland.mods.commons.reflect.UnsafeStrategy")
						.newInstance();
			} catch (Exception e) {
				// then not
			}
		}

		logger.warning("Using slow Strategy! This may lead to performance penalties. Please use Oracle's VM.");

		return new PureJavaStrategy();
	}

}
