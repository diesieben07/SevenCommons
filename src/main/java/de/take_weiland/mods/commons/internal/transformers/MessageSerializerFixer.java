package de.take_weiland.mods.commons.internal.transformers;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class MessageSerializerFixer extends ClassVisitor {

   private boolean hasAccept = false;

    public MessageSerializerFixer(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (name.equals("acceptOutboundMessage") && desc.equals(Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class)))) {
            hasAccept = true;
            return new AcceptMessageTransformer(mv);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        if (!hasAccept) {
            MethodVisitor mv = super.visitMethod(ACC_PUBLIC, "acceptOutboundMessage", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class)), null, null);
            if (mv != null) {
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(INSTANCEOF, "net/minecraft/network/Packet");
                mv.visitInsn(IRETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }

        super.visitEnd();
    }

    private static final class AcceptMessageTransformer extends MethodVisitor {

        public AcceptMessageTransformer(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            super.visitVarInsn(ALOAD, 1);
            super.visitTypeInsn(INSTANCEOF, "net/minecraft/network/Packet");

            Label dontBail = new Label();
            super.visitJumpInsn(IFNE, dontBail);
            super.visitInsn(ICONST_0);
            super.visitInsn(IRETURN);
            super.visitLabel(dontBail);
        }
    }
}
