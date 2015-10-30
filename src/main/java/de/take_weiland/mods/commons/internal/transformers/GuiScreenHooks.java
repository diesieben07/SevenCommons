package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.GuiScreenProxy;
import de.take_weiland.mods.commons.internal.SRGConstants;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class GuiScreenHooks extends ClassVisitor {

    private static final String FIELD_NAME = "_sc$textFields";

    public GuiScreenHooks(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (interfaces == null) {
            interfaces = new String[]{GuiScreenProxy.CLASS_NAME};
        } else {
            interfaces = ObjectArrays.concat(GuiScreenProxy.CLASS_NAME, interfaces);
        }

        super.visit(version, access, name, signature, superName, interfaces);


        String fieldDesc = Type.getDescriptor(List.class);
        FieldVisitor fv = super.visitField(ACC_PRIVATE | ACC_FINAL, FIELD_NAME, fieldDesc, null, null);
        if (fv != null) {
            fv.visitEnd();
        }

        String methodDesc = Type.getMethodDescriptor(Type.getType(List.class));
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, GuiScreenProxy.GET, methodDesc, null, null);
        if (mv != null) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, FIELD_NAME, fieldDesc);
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>")) {
            mv = new ConstructorTransformer(mv);
        } else if (name.equals(MCPNames.method(SRGConstants.M_HANDLE_INPUT))) {
            mv = new HandleInputTransformer(mv);
        }
        return mv;
    }

    private static class ConstructorTransformer extends MethodVisitor {

        private boolean foundInvokeSpecial;

        ConstructorTransformer(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);

            if (!foundInvokeSpecial && opcode == INVOKESPECIAL) {
                super.visitVarInsn(ALOAD, 0);
                super.visitTypeInsn(NEW, Type.getInternalName(ArrayList.class));
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(ArrayList.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
                super.visitFieldInsn(PUTFIELD, "net/minecraft/client/gui/GuiScreen", FIELD_NAME, Type.getDescriptor(List.class));

                foundInvokeSpecial = true;
            }
        }
    }

    private static class HandleInputTransformer extends MethodVisitor {

        HandleInputTransformer(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            String methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType("net/minecraft/client/gui/GuiScreen"));
            if (name.equals(MCPNames.method(SRGConstants.M_HANDLE_KEYBOARD_INPUT))) {
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESTATIC, ASMHooks.CLASS_NAME, ASMHooks.ON_GUI_KEY, methodDesc, false);
            } else if (name.equals(MCPNames.method(SRGConstants.M_HANDLE_MOUSE_INPUT))) {
                super.visitInsn(DUP);
                super.visitMethodInsn(INVOKESTATIC, ASMHooks.CLASS_NAME, ASMHooks.ON_GUI_MOUSE, methodDesc, false);
            }

            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
