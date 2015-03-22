package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.internal.sync.CompanionObjects;
import de.take_weiland.mods.commons.internal.sync.SyncedObjectProxy;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import org.objectweb.asm.*;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

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

        super.visit(V1_7, access, name, signature, superName, interfaces);
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

                Type callSiteType = Type.getType(CallSite.class);
                Type lookupType = Type.getType(MethodHandles.Lookup.class);
                Type stringType = Type.getType(String.class);
                Type methodTypeType = Type.getType(MethodType.class);
                Type classType = Type.getType(Class.class);
                Type syncerCompanionType = Type.getType(SyncCompanion.class);

                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 0);
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", Type.getMethodDescriptor(classType));

                Handle bootstrap = new Handle(H_INVOKESTATIC,
                        Type.getInternalName(CompanionObjects.class),
                        "bootstrap",
                        Type.getMethodDescriptor(callSiteType, lookupType, stringType, methodTypeType));

                super.visitInvokeDynamicInsn("doNotCare",
                        Type.getMethodDescriptor(syncerCompanionType, classType),
                        bootstrap);

                super.visitFieldInsn(PUTFIELD, className, COMPANION_FIELD, syncerCompanionType.getDescriptor());
            }
        }
    }
}
