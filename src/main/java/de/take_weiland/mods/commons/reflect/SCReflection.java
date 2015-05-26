package de.take_weiland.mods.commons.reflect;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.common.reflect.Reflection;
import de.take_weiland.mods.commons.OverrideSetter;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.internal.SevenCommonsLoader;
import de.take_weiland.mods.commons.internal.reflect.MethodHandleStrategy;
import de.take_weiland.mods.commons.internal.reflect.ReflectionStrategy;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import scala.reflect.ScalaLongSignature;
import scala.reflect.ScalaSignature;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * <p>Reflection utilities.</p>
 * <p>Among other things this class provides a way to access private fields, methods and classes without the performance
 * penalty of traditional reflection and while maintaining type-safety as much as possible. See {@link #createAccessor(Class)}.</p>
 */
@ParametersAreNonnullByDefault
public final class SCReflection {

    private static final Logger logger = SevenCommonsLoader.scLogger("Reflection");
    private static final ReflectionStrategy strategy = selectStrategy();
    private static final AtomicInteger nextDynClassId = new AtomicInteger(0);
    private static final boolean DEBUG = Boolean.getBoolean("sevencommons.reflect.debug");

    /**
     * <p>Create an object that implements the given Accessor Interface. The result of this method should be permanently cached,
     * because this operation is likely to be relatively expensive.</p>
     * <p>The given class must be an interface and must not extend any other interfaces.
     * All abstract methods must be marked with one of {@link de.take_weiland.mods.commons.reflect.Getter},
     * {@link de.take_weiland.mods.commons.reflect.Setter}, {@link de.take_weiland.mods.commons.reflect.Invoke} or {@link de.take_weiland.mods.commons.reflect.Construct}
     * and comply with the respective contract.</p>
     *
     * @param clazz the Accessor Interface
     * @return a newly created object, implementing the given interface
     */
    public static <T> T createAccessor(Class<T> clazz) {
        try {
            return strategy.createAccessor(clazz);
        } catch (IllegalAccessorException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalAccessorException("Failed to parse accessor interface " + clazz.getName(), e);
        }
    }

    /**
     * <p>Will try to find the corresponding setter method to the given method.</p>
     * <p>If the method is annotated with {@link OverrideSetter} the method specified there will be returned,
     * provided it is a valid setter.</p>
     * <p>Otherwise the following rules apply:</p>
     * <ul>
     *     <li>{@code getFoo} ⇒ {@code setFoo}</li>
     *     <li>{@code isFoo} ⇒ {@code setFoo}</li>
     *     <li>{@code foo} ⇒ {@code foo_$eq} (if the class is {@linkplain #isScala(Class) likely a scala class})</li>
     *     <li>{@code foo} ⇒ {@code foo}</li>
     * </ul>
     * @param getter the getter method
     * @return the setter method or {@code null} if no setter was found
     */
    public static Method findSetter(Method getter) {
        checkArgument(getter.getParameterTypes().length == 0, "Getters cannot take parameters");
        Class<?> type = getter.getReturnType();
        checkArgument(type != void.class, "Getters must not return void");

        String setterName;
        OverrideSetter ann = getter.getAnnotation(OverrideSetter.class);
        if (ann != null) {
            setterName = ann.value();
        } else {
            String getterName = getter.getName();
            if (getterName.startsWith("get") && getterName.length() >= 4 && Character.isUpperCase(getterName.charAt(3))) {
                setterName = "set" + getterName.substring(3);
            } else if (getterName.startsWith("is") && getterName.length() >= 3 && Character.isUpperCase(getterName.charAt(2))) {
                setterName = "set" + getterName.substring(2);
            } else if (isScala(getter.getDeclaringClass())) {
                setterName = getterName + "_$eq";
            } else {
                setterName = getterName;
            }
        }

        Method setter;
        try {
            setter = getter.getDeclaringClass().getDeclaredMethod(setterName, type);
        } catch (NoSuchMethodException e) {
            // no setter
            return null;
        }
        if (setter.getReturnType() != void.class) {
            return null;
        }
        return setter;
    }

    /**
     * <p>Tries to determine if the given class is a scala class.</p>
     * <p>If this method returns true, the class is most definitely a scala class. If it however returns false,
     * that is not an indication that the class is <i>not</i> a scala class.</p>
     *
     * @param clazz the class to check
     * @return true if the class is a scala class
     */
    public static boolean isScala(Class<?> clazz) {
        return clazz.isAnnotationPresent(ScalaSignature.class) || clazz.isAnnotationPresent(ScalaLongSignature.class);
    }

    /**
     * <p>Define a new Class specified by the given bytes.</p>
     *
     * @param bytes the bytes describing the class
     * @return the defined class
     */
    public static Class<?> defineDynamicClass(byte[] bytes) {
        return defineDynamicClass(bytes, Launch.classLoader);
    }

    /**
     * <p>Define a new Class specified by the given bytes.</p>
     *
     * @param bytes   the bytes describing the class
     * @param loader the class loader to use
     * @return the defined class
     */
    public static Class<?> defineDynamicClass(byte[] bytes, ClassLoader loader) {
        if (DEBUG) {
            try {
                ClassNode node = ASMUtils.getThinClassNode(bytes);
                File file = new File("sevencommonsdyn/" + node.name + ".class");
                Files.createParentDirs(file);
                try (OutputStream out = new FileOutputStream(file)) {
                    out.write(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Class<?> clazz;
        try {
            clazz = (Class<?>) CLASS_LOADER_DEFINE.invokeExact(loader, (String) null, bytes, 0, bytes.length);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
        Reflection.initialize(clazz);
        return clazz;
    }

    private static final MethodHandle CLASS_LOADER_DEFINE;

    static {
        try {
            Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            define.setAccessible(true);
            CLASS_LOADER_DEFINE = publicLookup().unreflect(define);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find defineClass in ClassLoader!");
        } catch (IllegalAccessException e) {
            throw new AssertionError("impossible");
        }
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
        return ASMUtils.internalName(pkg) + "/_sc_dyn_" + nextDynClassId.getAndIncrement();
    }

    private static ReflectionStrategy selectStrategy() {
        return new MethodHandleStrategy();
    }

    private SCReflection() {
    }

}
