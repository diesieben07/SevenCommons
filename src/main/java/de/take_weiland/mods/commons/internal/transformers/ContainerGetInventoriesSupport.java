package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.ContainerProxy;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class ContainerGetInventoriesSupport extends ClassVisitor {

    private static final String FIELD_NAME = "_sc$inventories";

    public ContainerGetInventoriesSupport(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String newIface = Type.getInternalName(ContainerProxy.class);
        if (interfaces == null) {
            interfaces = new String[]{newIface};
        } else {
            interfaces = ObjectArrays.concat(newIface, interfaces);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(MCPNames.M_ADD_CRAFTING_TO_CRAFTERS))) {
            return new AddListenerTransformer(mv);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        String containerIntName = "net/minecraft/inventory/Container";
        Type immutableSetType = Type.getType(ImmutableSet.class);
        Type myType = Type.getObjectType(containerIntName);
        Type asmHooksType = Type.getType(ASMHooks.class);

        FieldVisitor fv = super.visitField(ACC_PRIVATE, FIELD_NAME, immutableSetType.getDescriptor(), null, null);
        if (fv != null) {
            fv.visitEnd();
        }

        int access = ACC_PUBLIC | ACC_FINAL;
        String name = ContainerProxy.GET_INVENTORIES;
        String desc = Type.getMethodDescriptor(immutableSetType);
        MethodVisitor mv = super.visitMethod(access, name, desc, null, null);

        if (mv != null) {
            mv.visitCode();

            GeneratorAdapter gen = new GeneratorAdapter(mv, access, name, desc);

            Label nullPath = new Label();
            int temp = gen.newLocal(immutableSetType);

            gen.loadThis();
            gen.getField(myType, FIELD_NAME, immutableSetType);
            gen.storeLocal(temp);
            gen.loadLocal(temp);
            gen.ifNull(nullPath);
            gen.loadLocal(temp);
            gen.returnValue();

            gen.mark(nullPath);
            gen.loadThis();
            gen.invokeStatic(asmHooksType, new Method(ASMHooks.FIND_CONTAINER_INVS, immutableSetType, new Type[]{myType}));
            gen.storeLocal(temp);

            gen.loadThis();
            gen.loadLocal(temp);
            gen.putField(myType, FIELD_NAME, immutableSetType);

            gen.loadLocal(temp);
            gen.returnValue();

            gen.endMethod();
        }

        super.visitEnd();
    }

    private static final class AddListenerTransformer extends MethodVisitor {

        AddListenerTransformer(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitVarInsn(ALOAD, 0);
            super.visitVarInsn(ALOAD, 1);

            Type containerType = Type.getObjectType("net/minecraft/inventory/Container");
            Type iCraftingType = Type.getObjectType("net/minecraft/inventory/ICrafting");

            String owner = Type.getInternalName(ASMHooks.class);
            String name = ASMHooks.ON_LISTENER_ADDED;
            String desc = Type.getMethodDescriptor(Type.VOID_TYPE, containerType, iCraftingType);
            super.visitMethodInsn(INVOKESTATIC, owner, name, desc);
        }
    }
}
