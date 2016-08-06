package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanions;
import de.take_weiland.mods.commons.internal.sync_olds.SyncedObjectProxy;
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
        String newIFace = SyncedObjectProxy.CLASS_NAME;
        if (interfaces == null) {
            interfaces = new String[]{newIFace};
        } else {
            interfaces = ObjectArrays.concat(newIFace, interfaces);
        }
        className = name;

        super.visit(V1_7, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>")) {
            return new ConstructorTransformer(mv, className);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        String desc = Type.getDescriptor(SyncCompanion.class);
        FieldVisitor fv = super.visitField(ACC_PUBLIC | ACC_FINAL, COMPANION_FIELD, desc, null, null);
        if (fv != null) {
            fv.visitEnd();
        }

        int access = ACC_PUBLIC | ACC_FINAL;
        String name = SyncedObjectProxy.GET_COMPANION;
        desc = Type.getMethodDescriptor(Type.getType(SyncCompanion.class));

        MethodVisitor mv = super.visitMethod(access, name, desc, null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, COMPANION_FIELD, Type.getDescriptor(SyncCompanion.class));
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }

    static final class ConstructorTransformer extends MethodVisitor {

        private final String className;
        private boolean done = false;

        ConstructorTransformer(MethodVisitor mv, String className) {
            super(ASM4, mv);
            this.className = className;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (!done && opcode == INVOKESPECIAL && !owner.equals(className) /* skip constructors that delegate to other cstrs */) {
                done = true;

                Type classType = Type.getType(Class.class);
                Type syncCompanionType = Type.getType(SyncCompanion.class);

                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 0);
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", Type.getMethodDescriptor(classType), false);

                super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(SyncCompanions.class),
                        "newCompanion",
                        Type.getMethodDescriptor(syncCompanionType, classType), false);

                super.visitFieldInsn(PUTFIELD, className, COMPANION_FIELD, syncCompanionType.getDescriptor());
            }
        }
    }
}
