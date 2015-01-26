package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author diesieben07
 */
final class ArrayHandler extends ToNBTHandler {

    private final ClassWithProperties clazz;
    private ToNBTHandler elementHandler;
    private ASMVariable elementsRead;
    private ASMVariable elementsWrite;
    private final LocalVariable index = new LocalVariable(Type.INT_TYPE);

    ArrayHandler(ClassWithProperties clazz, ASMVariable var) {
        super(var);
        this.clazz = clazz;
    }

    @Override
    void overrideVar(ASMVariable var) {
        throw new UnsupportedOperationException();
    }

    @Override
    void initialTransform(MethodNode readMethod, MethodNode writeMethod) {
        elementsRead = ASMVariables.arrayElement(var, index.get(readMethod));
        elementsWrite = ASMVariables.arrayElement(var, index.get(writeMethod));
        elementHandler = ToNBTHandler.create(clazz, elementsRead);
    }

    @Override
    CodePiece makeNBT(MethodNode writeMethod) {
        CodeBuilder cb = new CodeBuilder();
        elementHandler.overrideVar(elementsWrite);

        cb.add(index.set(writeMethod, CodePieces.constant(0)));
        cb.add(elementHandler.makeNBT(writeMethod));

        return cb.build();
    }

    @Override
    CodePiece consumeNBT(CodePiece nbt, MethodNode readMethod) {
        CodeBuilder cb = new CodeBuilder();
        elementHandler.overrideVar(elementsRead);

        cb.add(index.set(readMethod, CodePieces.constant(0)));
        cb.add(elementHandler.consumeNBT(nbt, readMethod));

        return cb.build();
    }
}
