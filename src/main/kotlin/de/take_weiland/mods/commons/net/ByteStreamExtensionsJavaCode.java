package de.take_weiland.mods.commons.net;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.io.DataInput;
import java.io.DataOutput;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * @author diesieben07
 */
final class ByteStreamExtensionsJavaCode {

    private static final MethodHandle nbtWrite, nbtRead;

    static {
        try {
            nbtWrite = publicLookup().unreflect(ReflectionHelper.findMethod(NBTBase.class, "write", "func_74734_a", DataOutput.class));
            nbtRead = publicLookup().unreflect(ReflectionHelper.findMethod(NBTBase.class, "read", "func_152446_a", DataInput.class, int.class, NBTSizeTracker.class));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static void write(NBTBase nbt, DataOutput out) throws Throwable {
        nbtWrite.invokeExact(nbt, out);
    }

    static void read(NBTBase nbt, DataInput in, int depth, NBTSizeTracker sizeTracker) throws Throwable {
        nbtRead.invokeExact(nbt, in, depth, sizeTracker);
    }

    static IBlockState parseAndApply(@Nonnull String propertyName, @Nonnull String propertyValue, @Nonnull IBlockState state, @Nonnull BlockStateContainer stateContainer) {
        IProperty<?> property = stateContainer.getProperty(propertyName);
        if (property != null) {
            return applyValue(property, propertyValue, state);
        } else {
            return state;
        }
    }

    @SuppressWarnings("Guava")
    private static <T extends Comparable<T>> IBlockState applyValue(IProperty<T> property, String propertyValue, IBlockState state) {
        Optional<T> parsed = property.parseValue(propertyValue);
        return parsed.isPresent() ? state.withProperty(property, parsed.get()) : state;
    }

    static void writeNeededProperties(@Nonnull ByteBuf buf, @Nonnull IBlockState fullState, @Nonnull IBlockState stateFromMeta) {
        for (IProperty<?> property : fullState.getPropertyKeys()) {
            writePropertyIfNeeded(buf, fullState, stateFromMeta, property);
        }
    }

    private static <T extends Comparable<T>> void writePropertyIfNeeded(ByteBuf buf, IBlockState fullState, IBlockState stateFromMeta, IProperty<T> property) {
        T fullValue = fullState.getValue(property);
        T metaValue = stateFromMeta.getValue(property);
        if (!Objects.equals(fullValue, metaValue)) {
            ByteStreamExtensionsKt.writeString(buf, property.getName());
            ByteStreamExtensionsKt.writeString(buf, property.getName(fullValue));
        }
    }

}
