package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.net.ModPacket;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class ModPacketCstrAdder extends ClassVisitor {

    public static final boolean isNeeded;

    static {
        boolean hasValidReflFact;
        try {
            Class<?> reflFactory = Class.forName("sun.reflect.ReflectionFactory");
            reflFactory.getDeclaredMethod("newConstructorForSerialization", Class.class, Constructor.class);
            reflFactory.getDeclaredMethod("getConstructorAccessor", Constructor.class);
            hasValidReflFact = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            hasValidReflFact = false;
        }
        isNeeded = !hasValidReflFact;
    }

    public ModPacketCstrAdder(ClassVisitor cv) {
        super(ASM4, cv);
    }

    private boolean applicable;
    private String superClass;
    private boolean foundDfltCstr = false;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.superClass = superName;
        if (superName == null || superName.equals("java/lang/Object")) {
            applicable = false;
        } else if (superName.equals("de/take_weiland/mods/commons/net/ModPacket")) {
            // fast path for direct inheritors
            applicable = true;
        } else {
            applicable = ClassInfo.of(ModPacket.class).isAssignableFrom(ClassInfo.of(superName));
        }

        if (applicable) {
            access = access & ~(ACC_PRIVATE | ACC_PROTECTED) | ACC_PUBLIC;
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("<init>") && desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE))) {
            foundDfltCstr = true;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        if (applicable && !foundDfltCstr) {
            MethodVisitor mv = super.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
            if (mv != null) {
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, superClass, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE));
                mv.visitInsn(RETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        super.visitEnd();
    }
}
