package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;

/**
 * @author diesieben07
 */
public class InventoryNumberKeysFix extends MethodVisitor {

    public InventoryNumberKeysFix(MethodVisitor mv) {
        super(ASM4, mv);
    }

    @Override
    public void visitCode() {
        super.visitCode();

        Type guiContainerType = Type.getObjectType("net/minecraft/client/gui/inventory/GuiContainer");
        Type slotType = Type.getObjectType("net/minecraft/inventory/Slot");
        Type asmHooksType = Type.getType(ASMHooks.class);

        Label after = new Label();

        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, guiContainerType.getInternalName(), MCPNames.field(MCPNames.F_GUICONTAINER_THE_SLOT), slotType.getDescriptor());
        super.visitMethodInsn(INVOKESTATIC, asmHooksType.getInternalName(), ASMHooks.IS_USEABLE_CLIENT, Type.getMethodDescriptor(BOOLEAN_TYPE, slotType));
        super.visitJumpInsn(IFNE, after);

        super.visitInsn(ICONST_0);
        super.visitInsn(IRETURN);

        super.visitLabel(after);
    }
}
