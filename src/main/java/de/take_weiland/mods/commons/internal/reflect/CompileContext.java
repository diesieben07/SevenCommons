package de.take_weiland.mods.commons.internal.reflect;

import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class CompileContext {

    private final String className;
    private final ClassWriter cw;
    private final Map<Object, Pair<String, Class<?>>> constants = new HashMap<>();
    private int counter;

    CompileContext(String className, ClassWriter cw) {
        this.className = className;
        this.cw = cw;
    }

    public ClassVisitor cw() {
        return cw;
    }

    public void pushAsConstant(MethodVisitor mv, Object obj, Class<?> type) {
        if (!constants.containsKey(obj)) {
            String id = nextID();
            constants.put(obj, Pair.of(id, type));
        }
        Pair<String, Class<?>> pair = constants.get(obj);
        mv.visitFieldInsn(GETSTATIC, className, pair.getLeft(), Type.getDescriptor(pair.getRight()));
        ASMUtils.convertTypes(mv, Type.getType(pair.getRight()), Type.getType(type));
    }

    public static Iterator<Object> data;

    public Class<?> link() {
        // fix order
        Map<Object, Pair<String, Class<?>>> c = ImmutableMap.copyOf(this.constants);

        synchronized (CompileContext.class) {
            data = c.keySet().iterator();

            for (Map.Entry<Object, Pair<String, Class<?>>> entry : c.entrySet()) {
                FieldVisitor fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, entry.getValue().getLeft(), Type.getDescriptor(entry.getValue().getRight()), null, null);
                fv.visitEnd();
            }

            MethodVisitor mv = cw.visitMethod(ACC_STATIC | ACC_PUBLIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
            mv.visitCode();

            for (Map.Entry<Object, Pair<String, Class<?>>> entry : c.entrySet()) {
                mv.visitFieldInsn(GETSTATIC, Type.getInternalName(CompileContext.class), "data", Type.getDescriptor(Iterator.class));
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Iterator.class), "next", Type.getMethodDescriptor(Type.getType(Object.class)), true);
                ASMUtils.convertTypes(mv, Type.getType(Object.class), Type.getType(entry.getValue().getRight()));
                mv.visitFieldInsn(PUTSTATIC, className, entry.getValue().getLeft(), Type.getDescriptor(entry.getValue().getRight()));
            }

            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();

            cw.visitEnd();
            byte[] classFile = cw.toByteArray();
            Class<?> clazz = SCReflection.defineClass(classFile);
            data = null;

            return clazz;

        }
    }

    private String nextID() {
        return "_sc$cst$" + (counter++);
    }
}
