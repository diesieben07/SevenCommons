package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.POP;

/**
 * @author diesieben07
 */
final class ArrayHandler extends ToNBTHandler {

    private final ClassWithProperties clazz;
    private ToNBTHandler elementHandler;
    private ASMVariable elementsRead;
    private LocalVariable indexRead;

    ArrayHandler(ClassWithProperties clazz, ASMVariable var) {
        super(var);
        this.clazz = clazz;
    }

    @Override
    void overrideVar(ASMVariable var) {
//        throw new UnsupportedOperationException();
        super.overrideVar(var);
    }

    @Override
    void initialTransform(MethodNode readMethod, MethodNode writeMethod) {
        indexRead = new LocalVariable(readMethod, Type.INT_TYPE);

        elementsRead = ASMVariables.arrayElement(var, indexRead.get());
        elementHandler = ToNBTHandler.create(clazz, elementsRead);
        elementHandler.initialTransform(readMethod, writeMethod);
    }

    @Override
    CodePiece makeNBT(MethodNode writeMethod) {
        LocalVariable index = new LocalVariable(writeMethod, Type.INT_TYPE);

        CodeBuilder cb = new CodeBuilder();
        ASMVariable cachedVar;
        if (var instanceof LocalVariable) {
            cachedVar = var;
        } else {
            cachedVar = new LocalVariable(writeMethod, var.getType());
            cb.add(cachedVar.set(var.get()));
        }

        elementHandler.overrideVar(ASMVariables.arrayElement(cachedVar, index.get()));

        LocalVariable nbtList = new LocalVariable(writeMethod, Type.getType(NBTTagList.class));
        cb.add(nbtList.set(CodePieces.instantiate(NBTTagList.class)));

        CodePiece elemNbt = elementHandler.makeNBT(writeMethod);
        CodePiece addNbt = CodePieces.invokeVirtual(NBTTagList.class, "appendTag", nbtList.get(), void.class,
                NBTBase.class, elemNbt);

        LocalVariable len = new LocalVariable(writeMethod, Type.INT_TYPE);
        cb.add(len.set(CodePieces.arrayLength(cachedVar.get())));

        CodePiece loop = CodePieces.forLoop(CodePieces.constant(0), len.get(), addNbt, index);

        cb.add(loop);

        cb.add(nbtList.get());

        return cb.build();
    }

    @Override
    CodePiece consumeNBT(CodePiece nbt, MethodNode readMethod) {
        return nbt.append(new InsnNode(POP));
    }
}
