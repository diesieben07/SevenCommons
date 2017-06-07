package de.take_weiland.mods.commons.net;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author diesieben07
 */
final class ByteStreamExtensionsJavaCode {

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
