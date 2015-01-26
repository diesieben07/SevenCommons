package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.nbt.NBTData;
import net.minecraft.nbt.NBTBase;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
final class EnumSetHandler extends ToNBTHandler {

    private final ClassWithProperties clazz;
    private ASMVariable esType;

    EnumSetHandler(ClassWithProperties clazz, ASMVariable var) {
        super(var);
        this.clazz = clazz;
    }

    @Override
    void initialTransform(MethodNode readMethod, MethodNode writeMethod) {
        String name = "_sc$tonbt$est$" + ClassWithProperties.identifier(var);
        String desc = Type.getDescriptor(Class.class);
        FieldNode typeField = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, name, desc, null, null);
        clazz.clazz.fields.add(typeField);
        esType = ASMVariables.of(clazz.clazz, typeField);

        CodePiece me = CodePieces.constant(Type.getObjectType(clazz.clazz.name));
        CodePiece type;
        if (var.isMethod()) {
            CodePiece method = CodePieces.invokeVirtual(Class.class, "getDeclaredMethod", me, Method.class,
                    String.class, var.rawName(),
                    Class[].class, new Class[0]);
            type = CodePieces.invokeVirtual(Method.class, "getGenericReturnType", method, java.lang.reflect.Type.class);
        } else {
            CodePiece field = CodePieces.invokeVirtual(Class.class, "getDeclaredField", me, Field.class,
                    String.class, var.rawName());
            type = CodePieces.invokeVirtual(Field.class, "getGenericType", field, java.lang.reflect.Type.class);
        }

        String memberName = clazz.clazz.name + "." + var.rawName();

        CodePiece getType = CodePieces.invokeStatic(ASMHooks.class, ASMHooks.FIND_ES_TYPE, Class.class,
                java.lang.reflect.Type.class, type,
                String.class, memberName);

        ASMUtils.initializeStatic(clazz.clazz, esType.set(getType));
    }

    @Override
    CodePiece makeNBT(MethodNode writeMethod) {
        return CodePieces.invokeStatic(NBTData.class, "writeEnumSet", NBTBase.class,
                EnumSet.class, var.get());
    }

    @Override
    CodePiece consumeNBT(CodePiece nbt, MethodNode readMethod) {
        return var.set(CodePieces.invokeStatic(NBTData.class, "readEnumSet", EnumSet.class,
                NBTBase.class, nbt,
                Class.class, esType.get()));
    }
}
