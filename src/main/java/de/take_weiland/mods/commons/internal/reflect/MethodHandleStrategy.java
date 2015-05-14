package de.take_weiland.mods.commons.internal.reflect;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
public final class MethodHandleStrategy extends ReflectionStrategy {

    @Override
    public <T> T createAccessor(Class<T> iface) {
        validateInterface(iface);

        String className = SCReflection.nextDynamicClassName(iface.getPackage());
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_7, ACC_FINAL, className, null, "java/lang/Object", new String[] { Type.getInternalName(iface) });

        GeneratorAdapter gen = new GeneratorAdapter(0, getMethod("void <init>()"), null, null, cw);
        gen.visitCode();
        gen.loadThis();
        gen.invokeConstructor(Type.getType(Object.class), getMethod("void <init>()"));
        gen.returnValue();
        gen.endMethod();

        ImmutableMap<Method, MethodHandle> handles = FluentIterable.from(Arrays.asList(iface.getDeclaredMethods()))
                .toMap(AccessorMemberParser::getTarget);

        Type myType = Type.getObjectType(className);
        Type mhType = Type.getType(MethodHandle.class);

        int idx = 0;
        for (Map.Entry<Method, MethodHandle> entry : handles.entrySet()) {
            Method method = entry.getKey();
            MethodHandle methodHandle = entry.getValue();

            gen = new GeneratorAdapter(ACC_PUBLIC, getMethod(method), null, null, cw);
            gen.visitCode();

            gen.getStatic(myType, "h" + idx, mhType);

            Type[] paramTypes = new Type[methodHandle.type().parameterCount()];
            Type retType = Type.getType(methodHandle.type().returnType());

            for (int param = 0; param < methodHandle.type().parameterCount(); param++) {
                paramTypes[param] = Type.getType(methodHandle.type().parameterType(param));
                gen.loadArg(param);
            }

            gen.invokeVirtual(mhType, new org.objectweb.asm.commons.Method("invokeExact", retType, paramTypes));
            gen.returnValue();
            gen.endMethod();

            idx++;
        }

        Type iteratorType = Type.getType(Iterator.class);

        gen = new GeneratorAdapter(ACC_STATIC | ACC_PUBLIC, getMethod("void <clinit>()"), null, null, cw);
        gen.visitCode();

        for (idx = 0; idx < handles.size(); idx++) {
            gen.getStatic(Type.getType(MethodHandleStrategy.class), "staticData", iteratorType);
            gen.invokeInterface(iteratorType, getMethod("Object next()"));
            gen.checkCast(mhType);
            gen.putStatic(myType, "h" + idx, mhType);
        }
        gen.returnValue();
        gen.endMethod();

        for (idx = 0; idx < handles.size(); idx++){
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "h" + idx, mhType.getDescriptor(), null, null);
        }

        cw.visitEnd();

        synchronized (MethodHandleStrategy.class) {
            staticData = handles.values().iterator();
            try {
                Class<?> genClass = SCReflection.defineDynamicClass(cw.toByteArray());
                Constructor<?> cstr = genClass.getDeclaredConstructor();
                cstr.setAccessible(true);
                //noinspection unchecked
                return (T) cstr.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            } finally {
                staticData = null;
            }
        }
    }

    public static Iterator<?> staticData;

}
