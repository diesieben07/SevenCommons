package de.take_weiland.mods.commons.internal.reflect;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;
import de.take_weiland.mods.commons.asm.ASMContext;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class CompileContextImpl implements ASMContext {

    private final String className;
    private final ClassWriter cw;
    private final Map<Object, Constant> fieldConstants = new HashMap<>();
    private final List<Constant> lateConstants = new ArrayList<>();
    private int constantCounter;
    private final List<Consumer<? super MethodVisitor>> clinitPre, clinitPost;

    public CompileContextImpl(String className, ClassWriter cw) {
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
    public void pushConstant(MethodVisitor mv, @Nullable Object obj, Class<?> type) {
        if (obj == null) {
            mv.visitInsn(ACONST_NULL);
            return;
        }

        // translate direct method handles to ASM Handles to produce direct MH constant pool entries
        // instead of a static-final field
        if (obj instanceof MethodHandle) {
            MethodHandle mh = (MethodHandle) obj;
            try {
                MethodHandleInfo info = MethodHandles.publicLookup().revealDirect(mh);
                obj = new Handle(
                        info.getReferenceKind(),
                        Type.getInternalName(info.getDeclaringClass()),
                        info.getName(),
                        info.getMethodType().toMethodDescriptorString()
                );
            } catch (Exception e) {
                // ignored
            }
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
            if (i >= -1 && i <= 5) {
                mv.visitInsn(ICONST_0 + i);
            } else if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
                mv.visitIntInsn(BIPUSH, i);
            } else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
                mv.visitIntInsn(SIPUSH, i);
            } else {
                mv.visitLdcInsn(i);
            }
            actualType = Primitives.unwrap(obj.getClass());
        } else {
            if (!fieldConstants.containsKey(obj)) {
                String id = nextConstantID();
                Object of = obj;
                fieldConstants.put(obj, new Constant(id, type, c -> of));
            }
            Constant constant = fieldConstants.get(obj);
            mv.visitFieldInsn(GETSTATIC, className, constant.fieldName, Type.getDescriptor(constant.type));
            actualType = constant.type;
        }
        ASMUtils.convertTypes(mv, Type.getType(actualType), Type.getType(type));
    }

    @Override
    public <T> void pushLateConstant(MethodVisitor mv, Function<Class<?>, ? extends T> producer, Class<T> type) {
        String id = nextConstantID();
        lateConstants.add(new Constant(id, type, producer));
        mv.visitFieldInsn(GETSTATIC, className, id, Type.getDescriptor(type));
    }

    private static List<Constant> data;

    public static Iterator<?> data(Class<?> clazz) {
        return data.stream().map(constant -> constant.function.apply(clazz)).iterator();
    }

    @Override
    public Class<?> link(ClassLoader cl) {
        synchronized (CompileContextImpl.class) {
            // freeze ordering
            Map<Object, Constant> frozenConstants = ImmutableMap.copyOf(this.fieldConstants);
            Iterable<Constant> constants = Iterables.concat(frozenConstants.values(), lateConstants);

            data = new ArrayList<>();

            for (Constant constant : constants) {
                cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, constant.fieldName, Type.getDescriptor(constant.type), null, null);
                data.add(constant);
            }

            MethodVisitor mv = cw.visitMethod(ACC_STATIC | ACC_PUBLIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
            mv.visitCode();

            for (Consumer<? super MethodVisitor> consumer : clinitPre) {
                consumer.accept(mv);
            }

            mv.visitLdcInsn(Type.getObjectType(className));
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(CompileContextImpl.class), "data", Type.getMethodDescriptor(Type.getType(Iterator.class), Type.getType(Class.class)), false);

            for (Constant constant : constants) {
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Iterator.class), "next", Type.getMethodDescriptor(Type.getType(Object.class)), true);
                ASMUtils.convertTypes(mv, Type.getType(Object.class), Type.getType(constant.type));
                mv.visitFieldInsn(PUTSTATIC, className, constant.fieldName, Type.getDescriptor(constant.type));
            }
            mv.visitInsn(POP);

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

    private static final class Constant {

        final String fieldName;
        final Class<?> type;
        final Function<Class<?>, ?> function;

        Constant(String fieldName, Class<?> type, Function<Class<?>, ?> function) {
            this.fieldName = fieldName;
            this.type = type;
            this.function = function;
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
