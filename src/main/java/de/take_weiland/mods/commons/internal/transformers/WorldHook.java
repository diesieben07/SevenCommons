package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.WorldProxy;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class WorldHook extends ClassVisitor {

    static final String WORLD_CLASS_NAME = "net/minecraft/world/World";

    WorldHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        interfaces = ArrayUtils.add(interfaces, Type.getInternalName(WorldProxy.class));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        String mDesc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.INT_TYPE, Type.INT_TYPE);
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, WorldProxy.DOES_CHUNK_EXIST, mDesc, null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, WORLD_CLASS_NAME, MCPNames.method(SRGConstants.M_WORLD_CHUNK_EXISTS), mDesc, false);
            mv.visitInsn(IRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }
}
