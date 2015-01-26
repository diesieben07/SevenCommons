package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.MCPNames;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author diesieben07
 */
final class IntrinsicArraysHandler extends ToNBTHandler {

    private static final String INT_ARR = "[I";
    private static final String BOOL_ARR = "[Z";

    IntrinsicArraysHandler(ASMVariable var) {
        super(var);
    }

    static boolean isIntrinsic(String internalName) {
        return internalName.equals(INT_ARR) || internalName.equals(BOOL_ARR);

    }

    @Override
    CodePiece makeNBT(MethodNode writeMethod) {
        if (var.getType().getInternalName().equals(INT_ARR)) {
            return CodePieces.instantiate(NBTTagIntArray.class, String.class, "", int[].class, var.get());
        } else {
            return CodePieces.instantiate(NBTTagByteArray.class, String.class, "", byte[].class, var.get());
        }
    }

    @Override
    CodePiece consumeNBT(CodePiece nbt, MethodNode readMethod) {
        if (var.getType().getInternalName().equals(INT_ARR)) {
            CodePiece instance = CodePieces.castTo(NBTTagIntArray.class, nbt);
            String dataField = MCPNames.field(MCPNames.F_NBT_INT_ARR_DATA);
            return CodePieces.getField(Type.getInternalName(NBTTagIntArray.class), dataField, Type.getType(int[].class), instance);
        } else {
            CodePiece instance = CodePieces.castTo(NBTTagByteArray.class, nbt);
            String dataField = MCPNames.field(MCPNames.F_NBT_BYTE_ARR_DATA);
            return CodePieces.getField(Type.getInternalName(NBTTagByteArray.class), dataField, Type.getType(byte[].class), instance);
        }
    }
}
