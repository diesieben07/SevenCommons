package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.EntityProxy;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * @author diesieben07
 */
public class EntitySyncPropsHooks extends ClassVisitor {

    private static final String ADD_EXT_PROP_METHOD = "registerExtendedProperties";

    public EntitySyncPropsHooks(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String iface = EntityProxy.CLASS_NAME;
        if (interfaces == null) {
            interfaces = new String[]{iface};
        } else {
            interfaces = ObjectArrays.concat(interfaces, iface);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(ADD_EXT_PROP_METHOD)) {
            return new AddExtPropsPatcher(mv);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        Type myType = Type.getObjectType("net/minecraft/entity/Entity");

        MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, EntityProxy.GET_IEEP_MAP, Type.getMethodDescriptor(Type.getType(Map.class)), null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, myType.getInternalName(), "extendedProperties", Type.getDescriptor(HashMap.class));
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private static final class AddExtPropsPatcher extends MethodVisitor {

        public AddExtPropsPatcher(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            String asmHooks = Type.getInternalName(ASMHooks.class);
            Type entityType = Type.getObjectType("net/minecraft/entity/Entity");
            Type entityPropsType = Type.getObjectType("net/minecraftforge/common/IExtendedEntityProperties");
            Type stringType = Type.getType(String.class);

            super.visitVarInsn(ALOAD, 0);
            super.visitVarInsn(ALOAD, 2);
            super.visitVarInsn(ALOAD, 1);
            super.visitMethodInsn(INVOKESTATIC, asmHooks, ASMHooks.ON_NEW_ENTITY_PROPS, Type.getMethodDescriptor(VOID_TYPE, entityType, entityPropsType, stringType), false);
        }
    }

}
