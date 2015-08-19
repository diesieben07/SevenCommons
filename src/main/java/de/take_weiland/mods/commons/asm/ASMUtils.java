package de.take_weiland.mods.commons.asm;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.lang.invoke.MethodType;

import static org.objectweb.asm.Opcodes.*;

/**
 * <p>A collection of utility methods for working with the ASM library.</p>
 */
@ParametersAreNonnullByDefault
public final class ASMUtils {

    private ASMUtils() {
    }

    private static boolean isAssignableFrom(Type a, Type b) {
        return ClassInfo.of(a).isAssignableFrom(ClassInfo.of(b));
    }

    private static void box(MethodVisitor mv, Type primitive) {
        if (primitive.getSort() == Type.VOID) {
            mv.visitInsn(ACONST_NULL);
        } else {
            Type boxed = boxedType(primitive);
            mv.visitMethodInsn(INVOKESTATIC, boxed.getInternalName(), "valueOf", Type.getMethodDescriptor(boxed, primitive), false);
        }
    }

    private static void unbox(MethodVisitor mv, Type primitive) {
        if (primitive.getSort() == Type.VOID) {
            mv.visitInsn(POP);
        } else {
            Type boxedT = boxedType(primitive);
            mv.visitMethodInsn(INVOKEVIRTUAL, boxedT.getInternalName(), primitive.getClassName() + "Value", Type.getMethodDescriptor(primitive), false);
        }
    }

    public static void convertTypes(MethodVisitor mv, Type from, Type to) {
        Type fromUnboxed = unboxedType(from);
        Type toUnboxed = unboxedType(to);

        if (to.equals(from)) {
            return;
        }

        if (toUnboxed.getSort() == Type.VOID || fromUnboxed.getSort() == Type.VOID) { // handle void specially
            popValue(mv, from); // get rid of a possible value on the stack
            pushDefault(mv, to); // push possible default value
        } else if (isPrimitive(from) && isPrimitive(to)) { // primitive -> primitive, not including void
            convertPrimitives(mv, from, to);
        } else if (isPrimitive(from)) {
            if (isPrimitiveWrapper(to)) { // primitive -> wrapper
                convertPrimitives(mv, from, toUnboxed);
                box(mv, toUnboxed);
            } else { // primitive -> some object
                box(mv, from);
                convertTypes(mv, boxedType(from), to);
            }
        } else if (isPrimitive(to)) {
            if (isPrimitiveWrapper(from)) { // wrapper -> primitive
                unbox(mv, fromUnboxed);
                convertPrimitives(mv, fromUnboxed, to);
            } else { // some object -> primitive
                Type toBoxed = boxedType(to);
                convertTypes(mv, from, toBoxed);
                unbox(mv, to);
            }
        } else if (isPrimitiveWrapper(from) && isPrimitiveWrapper(to)) { // wrapper -> wrapper
            unbox(mv, fromUnboxed);
            convertTypes(mv, fromUnboxed, toUnboxed);
            box(mv, toUnboxed);
        } else if (!isAssignableFrom(to, from)) { // arbitrary conversion, just do a cast if needed
            mv.visitTypeInsn(CHECKCAST, to.getInternalName());
        }
    }

    // no void!
    private static void convertPrimitives(MethodVisitor mv, Type from, Type to) {
        int toSort = to.getSort();
        int fromSort = from.getSort();
        if (toSort == fromSort) {
            return;
        }

        switch (fromSort) {
            case Type.DOUBLE:
                switch (toSort) {
                    case Type.FLOAT:
                        mv.visitInsn(D2F);
                        break;
                    case Type.LONG:
                        mv.visitInsn(D2L);
                        break;
                    default:
                        mv.visitInsn(D2I);
                        convertTypes(mv, Type.INT_TYPE, to);
                        break;
                }
                break;
            case Type.FLOAT:
                switch (toSort) {
                    case Type.DOUBLE:
                        mv.visitInsn(F2D);
                        break;
                    case Type.LONG:
                        mv.visitInsn(F2L);
                        break;
                    default:
                        mv.visitInsn(F2I);
                        convertTypes(mv, Type.INT_TYPE, to);
                        break;
                }
                break;
            case Type.LONG:
                switch (toSort) {
                    case Type.DOUBLE:
                        mv.visitInsn(L2D);
                        break;
                    case Type.FLOAT:
                        mv.visitInsn(L2F);
                        break;
                    default:
                        mv.visitInsn(L2I);
                        convertTypes(mv, Type.INT_TYPE, to);
                }
                break;
            default:
                switch (toSort) {
                    case Type.DOUBLE:
                        mv.visitInsn(I2D);
                        break;
                    case Type.FLOAT:
                        mv.visitInsn(I2F);
                        break;
                    case Type.LONG:
                        mv.visitInsn(I2L);
                        break;
                    case Type.SHORT:
                        mv.visitInsn(I2S);
                        break;
                    case Type.CHAR:
                        mv.visitInsn(I2C);
                        break;
                    case Type.BYTE:
                    case Type.BOOLEAN:
                        mv.visitInsn(I2B);
                        break;
                }
        }
    }

    private static void pushDefault(MethodVisitor mv, Type type) {
        switch (type.getSort()) {
            case Type.VOID:
                break;
            case Type.OBJECT:
                Type unboxed = unboxedType(type);
                if (unboxed != type) {
                    pushDefault(mv, unboxed);
                    box(mv, unboxed);
                } else {
                    mv.visitInsn(ACONST_NULL);
                }
                break;
            case Type.DOUBLE:
                mv.visitInsn(DCONST_0);
                break;
            case Type.FLOAT:
                mv.visitInsn(FCONST_0);
                break;
            case Type.LONG:
                mv.visitInsn(LCONST_0);
                break;
            default:
                mv.visitInsn(ICONST_0);
                break;
        }
    }

    private static void popValue(MethodVisitor mv, Type type) {
        switch (type.getSort()) {
            case Type.DOUBLE:
            case Type.LONG:
                mv.visitInsn(POP2);
                break;
            case Type.VOID:
                break;
            default:
                mv.visitInsn(POP);
        }
    }

    // *** name utilities *** //

    public static String getMethodDescriptor(MethodType type) {
        int pcount = type.parameterCount();

        Type[] asmTypes = new Type[pcount];
        for (int i = 0; i < pcount; i++) {
            asmTypes[i] = Type.getType(type.parameterType(i));
        }
        return Type.getMethodDescriptor(Type.getType(type.returnType()), asmTypes);
    }

    /**
     * <p>Convert the given binary name (e.g. {@code java.lang.Object$Subclass}) to an internal name (e.g. {@code java/lang/Object$Subclass}).</p>
     *
     * @param binaryName the binary name
     * @return the internal name
     */
    public static String internalName(String binaryName) {
        return binaryName.replace('.', '/');
    }

    /**
     * <p>Convert the given internal name to a binary name (opposite of {@link #internalName(String)}).</p>
     *
     * @param internalName the internal name
     * @return the binary name
     */
    public static String binaryName(String internalName) {
        return internalName.replace('/', '.');
    }

    private static Optional<IClassNameTransformer> nameTransformer;

    /**
     * <p>Get the active {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any.</p>
     *
     * @return the active transformer, or null if none
     */
    @Nullable
    public static synchronized IClassNameTransformer getClassNameTransformer() {
        if (nameTransformer == null) {
            nameTransformer = FluentIterable.from(Launch.classLoader.getTransformers())
                    .filter(IClassNameTransformer.class)
                    .first();
        }
        return nameTransformer.orNull();
    }

    /**
     * <p>Transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any. Returns the untransformed
     * name if no transformer is present.</p>
     *
     * @param untransformedName the un-transformed internal name of the class
     * @return the transformed internal name of the class
     */
    public static String transformName(String untransformedName) {
        IClassNameTransformer t = getClassNameTransformer();
        return internalName(t == null ? untransformedName : t.remapClassName(binaryName(untransformedName)));
    }

    /**
     * <p>Un-transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any. Returns
     * the transformed name if no transformer is present.</p>
     *
     * @param transformedName the transformed internal name of the class
     * @return the un-transformed internal name of the class
     */
    public static String untransformName(String transformedName) {
        IClassNameTransformer t = getClassNameTransformer();
        return internalName(t == null ? transformedName : t.unmapClassName(binaryName(transformedName)));
    }

    // *** Misc Utils *** //

    /**
     * <p>Create a ClassNode for the class represented by the given bytes.</p>
     *
     * @param bytes the class bytes
     * @return a ClassNode
     */
    public static ClassNode getClassNode(byte[] bytes) {
        return getClassNode(bytes, 0);
    }

    /**
     * <p>Create a ClassNode for the class represented by the given bytes.</p>
     *
     * @param bytes       the class bytes
     * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
     * @return a ClassNode
     */
    public static ClassNode getClassNode(byte[] bytes, int readerFlags) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode clazz = new ClassNode();
        reader.accept(clazz, readerFlags);
        return clazz;
    }

    private static final int THIN_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    /**
     * <p>Create a ClassNode for the class represented by the given bytes.</p>
     * <p>The ClassNode will only contain the rough outline of the class. It is read with {@code ClassReader.SKIP_CODE}, {@code ClassReader.SKIP_DEBUG}
     * {@code ClassReader.SKIP_FRAMES} set.</p>
     *
     * @param bytes the class bytes
     * @return a ClassNode
     */
    public static ClassNode getThinClassNode(byte[] bytes) {
        return getClassNode(bytes, THIN_FLAGS);
    }

    /**
     * <p>Checks if the given {@link org.objectweb.asm.Type} represents a primitive type or the void type.</p>
     *
     * @param type the type
     * @return true if the {@code Type} represents a primitive type or void
     */
    public static boolean isPrimitive(Type type) {
        return type.getSort() != Type.ARRAY && type.getSort() != Type.OBJECT && type.getSort() != Type.METHOD;
    }

    /**
     * <p>Checks if the given {@code Type} represents a primitive wrapper such as {@code Integer} or the {@code Void} type.</p>
     *
     * @param type the type
     * @return true if the {@code Type} represents a primitive wrapper
     */
    public static boolean isPrimitiveWrapper(Type type) {
        return unboxedType(type) != type;
    }

    /**
     * <p>Get a Type representing the unboxed version of the given primitive wrapper type. If the given type is not
     * a primitive wrapper, the type itself will be returned.</p>
     *
     * @param wrapper the wrapper
     * @return the unboxed version
     */
    public static Type unboxedType(Type wrapper) {
        if (isPrimitive(wrapper)) {
            return wrapper;
        }
        switch (wrapper.getInternalName()) {
            case "java/lang/Void":
                return Type.VOID_TYPE;
            case "java/lang/Boolean":
                return Type.BOOLEAN_TYPE;
            case "java/lang/Byte":
                return Type.BYTE_TYPE;
            case "java/lang/Short":
                return Type.SHORT_TYPE;
            case "java/lang/Character":
                return Type.CHAR_TYPE;
            case "java/lang/Integer":
                return Type.INT_TYPE;
            case "java/lang/Long":
                return Type.LONG_TYPE;
            case "java/lang/Float":
                return Type.FLOAT_TYPE;
            case "java/lang/Double":
                return Type.DOUBLE_TYPE;
            default:
                return wrapper;
        }
    }

    /**
     * <p>Get a Type representing the boxed version of the given primitive type. If the given type is not
     * a primitive, the type itself will be returned.</p>
     *
     * @param primitive the primitive
     * @return the boxed version
     */
    public static Type boxedType(Type primitive) {
        switch (primitive.getSort()) {
            case Type.VOID:
                return Type.getType(Void.class);
            case Type.BOOLEAN:
                return Type.getType(Boolean.class);
            case Type.BYTE:
                return Type.getType(Byte.class);
            case Type.SHORT:
                return Type.getType(Short.class);
            case Type.CHAR:
                return Type.getType(Character.class);
            case Type.INT:
                return Type.getType(Integer.class);
            case Type.LONG:
                return Type.getType(Long.class);
            case Type.FLOAT:
                return Type.getType(Float.class);
            case Type.DOUBLE:
                return Type.getType(Double.class);
            default:
                return primitive;
        }
    }

}
