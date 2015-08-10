package de.take_weiland.mods.commons.asm.info;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.MissingClassException;
import de.take_weiland.mods.commons.internal.exclude.ClassInfoSuperCache;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static java.lang.invoke.MethodHandles.publicLookup;
import static org.objectweb.asm.Opcodes.*;

/**
 * <p>Some information about a class, obtain via {@link ClassInfo#of(String)}, {@link ClassInfo#of(Class)} or {@link ClassInfo#of(org.objectweb.asm.tree.ClassNode)}.</p>
 *
 * @author diesieben07
 */
public abstract class ClassInfo extends HasModifiers {

    private ClassInfo zuper;

    // limit subclasses to this package
    ClassInfo() {
    }

    /**
     * <p>Create a {@code ClassInfo} representing the given class.</p>
     *
     * @param clazz the Class
     * @return a ClassInfo
     */
    public static ClassInfo of(Class<?> clazz) {
        return new ClassInfoReflect(clazz);
    }

    /**
     * <p>Create a {@code ClassInfo} representing the given ClassNode.</p>
     *
     * @param clazz the ClassNode
     * @return a ClassInfo
     */
    public static ClassInfo of(ClassNode clazz) {
        return new ClassInfoASM(clazz);
    }

    /**
     * <p>Create a {@code ClassInfo} representing the given class.</p>
     * <p>This method will try to avoid loading actual classes into the JVM, but will instead use the ASM library
     * to analyze the raw class bytes if possible.</p>
     *
     * @param className the internal or binary name representing the class
     * @return a ClassInfo
     * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
     */
    public static ClassInfo of(String className) {
        switch (className) {
            case "void":
            case "V":
                return of(void.class);
            case "boolean":
            case "Z":
                return of(boolean.class);
            case "byte":
            case "B":
                return of(byte.class);
            case "short":
            case "S":
                return of(short.class);
            case "char":
            case "C":
                return of(char.class);
            case "int":
            case "I":
                return of(int.class);
            case "long":
            case "J":
                return of(long.class);
            case "float":
            case "F":
                return of(float.class);
            case "double":
            case "D":
                return of(double.class);
            default:
                return fromObjectClassName(ASMUtils.binaryName(className));
        }
    }

    /**
     * <p>Create a {@code ClassInfo} representing the given Type.</p>
     * <p>This method will try to avoid loading actual classes into the JVM, but will instead use the ASM library
     * to analyze the raw class bytes if possible.</p>
     *
     * @param type a Type representing the class to load, must not be a method type
     * @return a ClassInfo
     * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
     */
    public static ClassInfo of(Type type) {
        switch (type.getSort()) {
            case Type.ARRAY:
                Type rootType = type.getElementType();
                int dimensions = type.getDimensions();
                return new ClassInfoArray(dimensions, of(rootType));
            case Type.METHOD:
                throw new IllegalArgumentException("Cannot create ClassInfo of a Method Type!");
            case Type.BOOLEAN:
                return of(boolean.class);
            case Type.BYTE:
                return of(byte.class);
            case Type.SHORT:
                return of(short.class);
            case Type.CHAR:
                return of(char.class);
            case Type.INT:
                return of(int.class);
            case Type.LONG:
                return of(long.class);
            case Type.FLOAT:
                return of(float.class);
            case Type.DOUBLE:
                return of(double.class);
            case Type.VOID:
                return of(void.class);
            default:
                return fromObjectClassName(type.getClassName());
        }
    }

    // className must be a binary name
    private static ClassInfo fromObjectClassName(String className) {
        Class<?> clazz;
        if ((clazz = findLoadedClass(Launch.classLoader, className)) != null) {
            return new ClassInfoReflect(clazz);
        } else {
            try {
                // the class is not loaded, try get it's bytes
                byte[] bytes = Launch.classLoader.getClassBytes(ASMUtils.binaryName(ASMUtils.untransformName(className)));
                // somehow we can't access the class bytes (happens for classes not on the LaunchClassLoader)
                // we try and load the class now
                if (bytes == null) {
                    return forceLoad(className);
                } else {
                    // we found the bytes, lets use them
                    return new ClassInfoASM(ASMUtils.getThinClassNode(bytes));
                }
            } catch (IOException e) {
                // something went wrong getting the class bytes. try and load it
                return forceLoad(className);
            }
        }
    }

    // don't use the accessor interface mechanic
    // this happens very early in startup
    private static final MethodHandle findLoadedClass;

    static {
        try {
            Method method = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            method.setAccessible(true);
            findLoadedClass = publicLookup().unreflect(method);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> findLoadedClass(ClassLoader cl, String name) {
        try {
            return (Class<?>) findLoadedClass.invokeExact(cl, name);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static ClassInfo forceLoad(String className) {
        try {
            return of(Class.forName(className, false, ClassInfo.class.getClassLoader()));
        } catch (Exception e) {
            throw new MissingClassException(className, e);
        }
    }

    /**
     * <p>Get all interfaces directly implemented by this class (equivalent to {@link Class#getInterfaces()}.</p>
     *
     * @return the interfaces implemented by this class
     */
    public abstract List<String> interfaces();

    /**
     * <p>Get the internal name of the superclass of this class.</p>
     *
     * @return the superclass, or null if this ClassInfo is an interface or represents {@code java/lang/Object}.
     */
    public abstract String superName();

    public boolean hasSuper() {
        return superName() != null;
    }

    /**
     * <p>Get the internal name of this class (e.g. {@code java/lang/Object}.</p>
     *
     * @return the internal name
     */
    public abstract String internalName();

    /**
     * <p>Get a {@code ClassInfo} representing the superclass of this class.</p>
     *
     * @return the superclass, or null if this class has no superclass (see {@link #superName()})
     */
    public ClassInfo superclass() {
        if (zuper != null) {
            return zuper;
        }
        if (superName() == null) {
            return null;
        }
        return (zuper = of(superName()));
    }

    /**
     * <p>Determine if the given class can be safely casted to this class (equivalent to {@link java.lang.Class#isAssignableFrom(Class)}.</p>
     * <p>Like {@link #of(String)} this method will try to avoid loading actual classes.</p>
     *
     * @param child the class to check for
     * @return true if the given class can be casted to this class
     */
    public final boolean isAssignableFrom(ClassInfo child) {
        // double dispatch
        return child.callRightAssignableFrom(this);
    }

    boolean callRightAssignableFrom(ClassInfo parent) {
        return parent.isAssignableFromNormal(this);
    }

    boolean isAssignableFromNormal(ClassInfo child) {
        // some cheap tests first
        String childName = child.internalName();
        String myName = internalName();

        if (childName.equals("java/lang/Object")) {
            // Object is only assignable to itself
            return myName.equals("java/lang/Object");
        }
        if (myName.equals("java/lang/Object") // everything is assignable to Object
                || childName.equals(myName) // we are the same
                || myName.equals(child.superName()) // we are the superclass of child
                || child.interfaces().contains(myName)) { // we are an interface that child implements
            return true;
        }

        // if we are a class no interface can be cast to us
        if (!isInterface() && child.isInterface()) {
            return false;
        }

        // final classes cannot be subclassed
        if (isFinal()) {
            return false;
        }

        // need to compute supers now
        return child.getSupers().contains(myName);
    }

    boolean isAssignableFromReflect(ClassInfoReflect child) {
        return isAssignableFromNormal(child);
    }

    /**
     * <p>Get all superclasses in the hierarchy chain of this class as well as all interfaces this class
     * implements directly or indirectly.</p>
     * <p>In other words return all classes that this class can be safely casted to.</p>
     *
     * @return an immutable Set containing all superclasses and interfaces
     */
    public Set<String> getSupers() {
        return ClassInfoSuperCache.getSupers(this);
    }

    /**
     * <p>Get the number of dimensions of this array class, or 0 if this ClassInfo does not represent an array class.</p>
     *
     * @return the number of dimensions
     */
    public abstract int getDimensions();

    /**
     * <p>Determine if this class is an array class (equivalent to {@link Class#isArray()}</p>
     *
     * @return true if this class is an array class
     */
    public boolean isArray() {
        return getDimensions() > 0;
    }

    /**
     * <p>Determine if this class is an interface.</p>
     *
     * @return true if this class is an interface
     */
    public boolean isInterface() {
        return hasModifier(ACC_INTERFACE);
    }

    /**
     * <p>Determine if this class is abstract</p>
     *
     * @return true if this class is abstract
     */
    public boolean isAbstract() {
        return hasModifier(ACC_ABSTRACT);
    }

    /**
     * <p>Determine if this class is an annotation.</p>
     *
     * @return true if this class is an annotation
     */
    public boolean isAnnotation() {
        return hasModifier(ACC_ANNOTATION);
    }

    /**
     * <p>Determine if this ClassInfo represents an enum class (equivalent to {@link Class#isEnum()}.</p>
     * <p>Note: Like the JDK method this method will return false for the classes generated for specialized enum constants.
     * Use {@code hasModifier(ACC_ENUM)} to include those explicitly.</p>
     *
     * @return true if this ClassInfo represents an enum class
     */
    public boolean isEnum() {
        return hasModifier(ACC_ENUM) && superName().equals("java/lang/Enum");
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ClassInfo && internalName().equals(((ClassInfo) o).internalName());
    }

    @Override
    public int hashCode() {
        return internalName().hashCode();
    }

    @Override
    public String toString() {
        return (isInterface() ? "interface" : isAbstract() ? "abstract class" : "class") + " " + internalName();
    }
}
