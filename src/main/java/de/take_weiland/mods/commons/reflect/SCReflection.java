package de.take_weiland.mods.commons.reflect;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.common.reflect.Reflection;
import de.take_weiland.mods.commons.OverrideSetter;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.SevenCommonsLoader;
import de.take_weiland.mods.commons.internal.reflect.MethodHandleStrategy;
import de.take_weiland.mods.commons.internal.reflect.ReflectionStrategy;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * <p>Reflection utilities.</p>
 * <p>This class provides an alternative to using traditional reflection to access inaccessible fields and methods.</p>
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
     * All methods must be marked with one of {@link de.take_weiland.mods.commons.reflect.Getter},
     * {@link de.take_weiland.mods.commons.reflect.Setter}, {@link de.take_weiland.mods.commons.reflect.Invoke} or {@link de.take_weiland.mods.commons.reflect.Construct}
     * and comply with the respective contract.</p>
     *
     * @param iface the Accessor Interface
     * @return a newly created object, implementing the given interface
     */
    public static <T> T createAccessor(Class<T> iface) {
        try {
            return strategy.createAccessor(iface);
        } catch (IllegalAccessorException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalAccessorException("Failed to parse accessor interface " + iface.getName(), e);
        }
    }

    public static Method findSetter(Method getter) {
        checkArgument(getter.getParameterTypes().length == 0);
        Class<?> type = getter.getReturnType();
        checkArgument(type != void.class);

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

        try {
            return getter.getDeclaringClass().getDeclaredMethod(setterName, type);
        } catch (NoSuchMethodException e) {
            // no setter
            return null;
        }
    }

    private static Class<? extends Annotation> scalaSigClass;


    private static Class<? extends Annotation> scalaLongSigClass;

    /**
     * <p>Tries to determine if the given class is a scala class.</p>
     * <p>If this method returns true, the class is most definitely a scala class. If it however returns false,
     * that is not an indication that the class is <i>not</i> a scala class.</p>
     *
     * @param clazz the class to check
     * @return true if the class is a scala class
     */
    public static boolean isScala(Class<?> clazz) {
        try {
            if (scalaSigClass == null) {
                scalaSigClass = Class.forName("scala.reflect.ScalaSignature")
                        .asSubclass(Annotation.class);

                scalaLongSigClass = Class.forName("scala.reflect.ScalaLongSignature")
                        .asSubclass(Annotation.class);
            }
        } catch (ClassNotFoundException e) {
            // ignored
        }
        // if these classes are not found use a dummy annotation that will never be present
        // to avoid checking Class.forName over and over again
        if (scalaSigClass == null) {
            scalaSigClass = Override.class;
        }
        if (scalaLongSigClass == null) {
            scalaLongSigClass = Override.class;
        }
        return clazz.isAnnotationPresent(scalaSigClass)
                || clazz.isAnnotationPresent(scalaLongSigClass)
                || ClassInfo.of(clazz).getSourceFile().endsWith(".scala");
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
     * <p>Define a new Class specified by the given bytes.</p>
     *
     * @param bytes the bytes describing the class
     * @return the defined class
     */
    public static Class<?> defineDynamicClass(byte[] bytes) {
        return defineDynamicClass(bytes, SCReflection.class);
    }

    /**
     * <p>Define a new Class specified by the given bytes.</p>
     *
     * @param bytes   the bytes describing the class
     * @param context the class which to use as the context
     * @return the defined class
     */
    public static Class<?> defineDynamicClass(byte[] bytes, Class<?> context) {
        if (DEBUG) {
            try {
                ClassNode node = ASMUtils.getThinClassNode(bytes);
                File file = new File("sevencommonsdyn/" + node.name + ".class");
                Files.createParentDirs(file);
                OutputStream out = new FileOutputStream(file);
                out.write(bytes);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ClassLoader cl = context.getClassLoader();
        Class<?> clazz;
        try {
            clazz = (Class<?>) CLASS_LOADER_DEFINE.invokeExact(cl, (String) null, bytes, 0, bytes.length);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
        Reflection.initialize(clazz);
        return clazz;
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
