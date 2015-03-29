package de.take_weiland.mods.commons.internal.sync.builtin;

import com.google.common.primitives.Primitives;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.sync.Syncer;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
final class PrimitiveAndBoxSyncerFactory {

    static <V> Syncer<V, V> createSyncer(Class<V> clazz) {
        if (clazz.isPrimitive() || Primitives.isWrapperType(clazz)) {
            return makeSyncer(clazz, !clazz.isPrimitive());
        } else {
            return null;
        }
    }

    static <V> Syncer<V, V> makeSyncer(Class<V> clazz, boolean box) {
        ClassWriter cw = newCW(clazz);
        Type boxed = Type.getType(Primitives.wrap(clazz));
        Type unboxed = Type.getType(Primitives.unwrap(clazz));

        Method method = getMethod("Object writeAndUpdate(Object, Object, de.take_weiland.mods.commons.net.MCDataOutput)");
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
        gen.loadArg(2);
        gen.loadArg(0);
        gen.checkCast(boxed);
        if (!box) {
            unbox(gen, boxed, unboxed);
        }
        Method writeMethod = new Method("write" + StringUtils.capitalize(unboxed.getClassName()) + (box ? "Box" : ""), VOID_TYPE, new Type[] {box ? boxed : unboxed});
        gen.invokeInterface(Type.getType(MCDataOutput.class), writeMethod);
        gen.loadArg(0);
        gen.returnValue();
        gen.endMethod();

        method = getMethod("Object read(Object, Object, de.take_weiland.mods.commons.net.MCDataInput)");
        gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
        gen.loadArg(2);
        Method readMethod = new Method("read" + StringUtils.capitalize(unboxed.getClassName()) + (box ? "Box" : ""), box ? boxed : unboxed, new Type[0]);
        gen.invokeInterface(Type.getType(MCDataInput.class), readMethod);
        if (!box) {
            box(gen, boxed, unboxed);
        }
        gen.returnValue();
        gen.endMethod();

        cw.visitEnd();

        Class<?> syncerClass = SCReflection.defineDynamicClass(cw.toByteArray());
        try {
            //noinspection unchecked
            return (Syncer<V, V>) syncerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e); // impossible
        }
    }

    static void unbox(GeneratorAdapter gen, Type boxed, Type unboxed) {
        String unboxMethod = unboxed.getClassName() + "Value";
        gen.invokeVirtual(boxed, new Method(unboxMethod, unboxed, new Type[0]));
    }

    static void box(GeneratorAdapter gen, Type boxed, Type unboxed) {
        gen.invokeStatic(boxed, new Method("valueOf", boxed, new Type[] { unboxed }));
    }

    static ClassWriter newCW(Class<?> clazz) {
        String className = SCReflection.nextDynamicClassName(PrimitiveAndBoxSyncerFactory.class.getPackage());
        String superName = Type.getInternalName(SyncerDefaultEquals.class);

        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_7, ACC_FINAL, className, null, superName, null);

        GeneratorAdapter gen = new GeneratorAdapter(0, getMethod("void <init>()"), null, null, cw);
        gen.loadThis();
        gen.push(Type.getType(clazz));
        gen.invokeConstructor(Type.getObjectType(superName), getMethod("void <init>(Class)"));
        gen.returnValue();
        gen.endMethod();

        return cw;
    }

    private PrimitiveAndBoxSyncerFactory() {}

}
