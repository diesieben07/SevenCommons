package de.take_weiland.mods.commons.internal.reflect;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import de.take_weiland.mods.commons.asm.ASMContext;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class CompileContextImpl implements ASMContext {

    private final String className;
    private final ClassWriter cw;
    private final Map<Object, Pair<String, Class<?>>> constants = new HashMap<>();
    private int constantCounter;
    private final List<Consumer<? super MethodVisitor>> clinitPre, clinitPost;

    CompileContextImpl(String className, ClassWriter cw) {
        this.className = className;
        this.cw = cw;
        clinitPre = new ArrayList<>();
        clinitPost = new ArrayList<>();
    }

    @Override
    public ClassWriter cw() {
        return cw;
    }

    @Override
    public void pushAsConstant(MethodVisitor mv, @Nullable Object obj, Class<?> type) {
        if (obj == null) {
            mv.visitInsn(ACONST_NULL);
            return;
        }
        Class<?> actualType;

        if (obj instanceof String || obj instanceof Type || obj instanceof Handle) {
            mv.visitLdcInsn(obj);
            actualType = obj instanceof String ? String.class : obj instanceof Type ? Class.class : MethodHandle.class;
        } else if (obj instanceof Float) {
            float f = (Float) obj;
            if (f == 0F) {
                mv.visitInsn(FCONST_0);
            } else if (f == 1F) {
                mv.visitInsn(FCONST_1);
            } else if (f == 2F) {
                mv.visitInsn(FCONST_2);
            } else {
                mv.visitLdcInsn(obj);
            }
            actualType = float.class;
        } else if (obj instanceof Double) {
            double d = (Double) obj;
            if (d == 0D) {
                mv.visitInsn(DCONST_0);
            } else if (d == 1D) {
                mv.visitInsn(DCONST_1);
            } else {
                mv.visitLdcInsn(obj);
            }
            actualType = double.class;
        } else if (obj instanceof Long) {
            long l = (Long) obj;
            if (l == 0L) {
                mv.visitInsn(LCONST_0);
            } else if (l == 1L) {
                mv.visitInsn(LCONST_1);
            } else {
                mv.visitLdcInsn(obj);
            }
            actualType = long.class;
        } else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Character) {
            int i = obj instanceof Number ? ((Number) obj).intValue() : (Character) obj;
            switch (i) {
                case -1:
                    mv.visitInsn(ICONST_M1);
                    break;
                case 0:
                    mv.visitInsn(ICONST_0);
                    break;
                case 1:
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    mv.visitInsn(ICONST_4);
                    break;
                case 5:
                    mv.visitInsn(ICONST_5);
                    break;
                default:
                    if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
                        mv.visitIntInsn(BIPUSH, i);
                    } else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
                        mv.visitIntInsn(SIPUSH, i);
                    } else {
                        mv.visitLdcInsn(i);
                    }
            }
            actualType = Primitives.unwrap(obj.getClass());
        } else {
            if (!constants.containsKey(obj)) {
                String id = nextConstantID();
                constants.put(obj, Pair.of(id, type));
            }
            Pair<String, Class<?>> pair = constants.get(obj);
            mv.visitFieldInsn(GETSTATIC, className, pair.getLeft(), Type.getDescriptor(pair.getRight()));
            actualType = pair.getRight();
        }
        ASMUtils.convertTypes(mv, Type.getType(actualType), Type.getType(type));
    }

    public static Iterator<Object> data;

    @Override
    public Class<?> link(ClassLoader cl) {
        // freeze ordering
        Map<Object, Pair<String, Class<?>>> c = ImmutableMap.copyOf(this.constants);

        synchronized (CompileContextImpl.class) {
            data = c.keySet().iterator();

            for (Map.Entry<Object, Pair<String, Class<?>>> entry : c.entrySet()) {
                FieldVisitor fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, entry.getValue().getLeft(), Type.getDescriptor(entry.getValue().getRight()), null, null);
                fv.visitEnd();
            }

            MethodVisitor mv = cw.visitMethod(ACC_STATIC | ACC_PUBLIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
            mv.visitCode();

            for (Consumer<? super MethodVisitor> consumer : clinitPre) {
                consumer.accept(mv);
            }

            for (Map.Entry<Object, Pair<String, Class<?>>> entry : c.entrySet()) {
                mv.visitFieldInsn(GETSTATIC, Type.getInternalName(CompileContextImpl.class), "data", Type.getDescriptor(Iterator.class));
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Iterator.class), "next", Type.getMethodDescriptor(Type.getType(Object.class)), true);
                ASMUtils.convertTypes(mv, Type.getType(Object.class), Type.getType(entry.getValue().getRight()));
                mv.visitFieldInsn(PUTSTATIC, className, entry.getValue().getLeft(), Type.getDescriptor(entry.getValue().getRight()));
            }

            for (Consumer<? super MethodVisitor> consumer : clinitPost) {
                consumer.accept(mv);
            }

            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();

            cw.visitEnd();

            byte[] classFile = cw.toByteArray();
            Class<?> clazz = SCReflection.defineClass(classFile, cl);

            data = null;

            return clazz;

        }
    }

    @Override
    public Object linkInstantiate(ClassLoader cl) {
        Class<?> cls = link(cl);
        try {
            Constructor<?> cstr = cls.getDeclaredConstructor();
            cstr.setAccessible(true);
            return cstr.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addStaticInitHook(Consumer<? super MethodVisitor> hook, boolean preConstantInit) {
        (preConstantInit ? clinitPre : clinitPost).add(hook);
    }

    private String nextConstantID() {
        return "_sc$cst$" + (constantCounter++);
    }
}
