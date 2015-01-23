package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.ASMVariables;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.MethodContext;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import net.minecraft.nbt.NBTTagList;
import org.objectweb.asm.Type;

/**
 * @author diesieben07
 */
final class ArrayHandler extends ToNBTHandler {

    private final ToNBTHandler elementHandler;

    ArrayHandler(ClassWithProperties clazz, ASMVariable var) {
        super(var);
        ASMVariable dummyElem = ASMVariables.arrayElement(var, 0);
        elementHandler = ToNBTHandler.create(clazz, dummyElem);
    }

    @Override
    CodePiece makeNBT(MethodContext context) {
        ASMVariable list = context.newLocal(Type.getType(NBTTagList.class));

        ASMVariable elem = ASMVariables.arrayElement(var, )
    }

    @Override
    CodePiece consumeNBT(CodePiece nbt) {
        return null;
    }
}
