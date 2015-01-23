package de.take_weiland.mods.commons.internal.transformers.tonbt;

import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.MethodContext;
import de.take_weiland.mods.commons.nbt.NBTData;
import net.minecraft.nbt.NBTBase;
import org.objectweb.asm.Type;

import java.util.Set;

/**
 * @author diesieben07
 */
final class SimpleIntrinsicsHandler extends ToNBTHandler {

    private static final Set<String> intrinsics = ImmutableSet.of(
            "java/lang/String",
            "java/util/UUID",
            "net/minecraft/block/Block",
            "net/minecraft/item/Item",
            "net/minecraft/item/ItemStack",
            "net/minecraftforge/fluids/FluidStack"
    );

    static boolean isIntrinsic(Type type) {
        return intrinsics.contains(type.getInternalName());
    }

    private final Type type;

    SimpleIntrinsicsHandler(Type type, ASMVariable var) {
        super(var);
        this.type = type;
    }

    @Override
    CodePiece makeNBT(MethodContext context) {
        return CodePieces.invokeStatic(NBTData.class, "write" + getSimpleName(), NBTBase.class,
                type, var.get());
    }

    @Override
    CodePiece consumeNBT(CodePiece nbt) {
        return var.set(CodePieces.invokeStatic(NBTData.class, "read" + getSimpleName(), type,
                NBTBase.class, nbt));
    }

    private String getSimpleName() {
        String internalName = type.getInternalName();
        int off = internalName.lastIndexOf('/');
        return internalName.substring(off + 1);
    }
}
