package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.EntityRendererProxy;
import de.take_weiland.mods.commons.internal.SRGConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class EntityRendererHook extends ClassVisitor {

    static final String ENTITY_RENDERER_CLASS = "net/minecraft/client/renderer/EntityRenderer";

    public EntityRendererHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        interfaces = ArrayUtils.add(interfaces, Type.getInternalName(EntityRendererProxy.class));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, EntityRendererProxy.GET_FOV_MOD_HAND, Type.getMethodDescriptor(Type.FLOAT_TYPE), null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, ENTITY_RENDERER_CLASS, MCPNames.field(SRGConstants.F_FOV_MODIFIER_HAND), Type.FLOAT_TYPE.getDescriptor());
            mv.visitInsn(FRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, EntityRendererProxy.SET_FOV_MOD_HAND, Type.getMethodDescriptor(Type.VOID_TYPE, Type.FLOAT_TYPE), null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(FLOAD, 1);
            mv.visitFieldInsn(PUTFIELD, ENTITY_RENDERER_CLASS, MCPNames.field(SRGConstants.F_FOV_MODIFIER_HAND), Type.FLOAT_TYPE.getDescriptor());
            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, EntityRendererProxy.GET_FOV_MOD_HAND_PREV, Type.getMethodDescriptor(Type.FLOAT_TYPE), null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, ENTITY_RENDERER_CLASS, MCPNames.field(SRGConstants.F_FOV_MODIFIER_HAND_PREV), Type.FLOAT_TYPE.getDescriptor());
            mv.visitInsn(FRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, EntityRendererProxy.SET_FOV_MOD_HAND_PREV, Type.getMethodDescriptor(Type.VOID_TYPE, Type.FLOAT_TYPE), null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(FLOAD, 1);
            mv.visitFieldInsn(PUTFIELD, ENTITY_RENDERER_CLASS, MCPNames.field(SRGConstants.F_FOV_MODIFIER_HAND_PREV), Type.FLOAT_TYPE.getDescriptor());
            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }
}
