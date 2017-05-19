package de.take_weiland.mods.commons.internal.transformers.net;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.net.simple.SimplePacket;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletableFuture;

import static org.objectweb.asm.Opcodes.*;

/**
 * <p>adds all the methods in SimplePacket to SimplePacket.WithResponse. This is in a transformer because java doesn't allow different return-type methods in the same
 * class, but the JVM does.</p>
 * @author diesieben07
 */
public class SimplePacketWithResponseTransformer extends ClassVisitor {

    public SimplePacketWithResponseTransformer(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String simplePacketIntName = "de/take_weiland/mods/commons/net/SimplePacket";
        if (interfaces == null || interfaces.length == 0) {
            interfaces = new String[]{simplePacketIntName};
        } else {
            interfaces = ObjectArrays.concat(simplePacketIntName, interfaces);
        }
        super.visit(version, access, name, signature, superName, interfaces);

        Class<SimplePacket> spClass = SimplePacket.class;
        for (Method method : spClass.getDeclaredMethods()) {
            if (method.isSynthetic() || method.isBridge() || Modifier.isStatic(method.getModifiers()) || method.getReturnType() != void.class) {
                continue;
            }

            MethodVisitor mv = super.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
            mv.visitCode();

            Type methodType = Type.getType(method);
            String descWithResponse = Type.getMethodDescriptor(Type.getType(CompletableFuture.class), methodType.getArgumentTypes());

            mv.visitVarInsn(ALOAD, 0);
            int pIdx = 1;
            Type[] params = methodType.getArgumentTypes();
            for (Type param : params) {
                mv.visitVarInsn(param.getOpcode(ILOAD), pIdx);
                pIdx += param.getSize();
            }
            mv.visitMethodInsn(INVOKEINTERFACE, name, method.getName(), descWithResponse, true);
//            mv.visitInsn(POP);
            // RETURN also POPs
            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
}
