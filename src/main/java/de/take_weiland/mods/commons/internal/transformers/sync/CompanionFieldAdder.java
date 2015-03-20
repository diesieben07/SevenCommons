package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.internal.sync.SyncedObjectProxy;
import de.take_weiland.mods.commons.internal.sync.CompanionObjects;
import de.take_weiland.mods.commons.internal.sync.SyncerCompanion;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class CompanionFieldAdder extends ClassVisitor {

    public static final String COMPANION_FIELD = "_sc$companion";

    String className;

    public CompanionFieldAdder(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String newIFace = Type.getInternalName(SyncedObjectProxy.class);
        if (interfaces == null) {
            interfaces = new String[] { newIFace };
        } else {
            interfaces = ObjectArrays.concat(newIFace, interfaces);
        }

        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>")) {
            return new ConstructorTransformer(mv);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        String desc = Type.getDescriptor(SyncerCompanion.class);
        FieldVisitor fv = super.visitField(ACC_PUBLIC | ACC_FINAL, COMPANION_FIELD, desc, null, null);
        if (fv != null) {
            fv.visitEnd();
        }

        int access = ACC_PUBLIC | ACC_FINAL;
        String name = SyncedObjectProxy.GET_COMPANION;
        desc = Type.getMethodDescriptor(Type.getType(SyncerCompanion.class));

        MethodVisitor mv = super.visitMethod(access, name, desc, null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, COMPANION_FIELD, Type.getDescriptor(SyncerCompanion.class));
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }

    private final class ConstructorTransformer extends MethodVisitor {

        private boolean done = false;

        ConstructorTransformer(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, owner, name, desc);
            if (!done && opcode == INVOKESPECIAL) {
                done = true;

                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 0);

                Type companionType = Type.getType(SyncerCompanion.class);
                Type objectType = Type.getType(Object.class);

                String _owner = Type.getInternalName(CompanionObjects.class);
                String _name = CompanionObjects.METHOD_NEW_COMPANION;
                String _desc = Type.getMethodDescriptor(companionType, objectType);
                super.visitMethodInsn(INVOKESTATIC, _owner, _name, _desc);

                super.visitFieldInsn(PUTFIELD, className, COMPANION_FIELD, companionType.getDescriptor());
            }
        }
    }
}
