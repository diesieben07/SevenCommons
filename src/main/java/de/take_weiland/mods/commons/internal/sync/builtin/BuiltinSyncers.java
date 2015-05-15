package de.take_weiland.mods.commons.internal.sync.builtin;

import com.google.common.primitives.Primitives;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.Property;
import de.take_weiland.mods.commons.sync.SyncCapacity;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public final class BuiltinSyncers implements SyncerFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <VAL> Syncer<VAL, ?, ?> getSyncer(Property<VAL, ?> property) {
        Class<? super VAL> type = property.getRawType();
        Syncer<VAL, ?, ?> syncer = null;

        if (property.getDesiredMethod() != SerializationMethod.Method.CONTENTS) {
            syncer = (Syncer<VAL, ?, ?>) getValueSyncer(type);
        }
        if (syncer == null && property.getDesiredMethod() != SerializationMethod.Method.VALUE) {
            syncer = (Syncer<VAL, ?, ?>) getContentsSyncer(type, property);
        }
        return syncer;
    }

    private static final Map<Class<?>, Syncer<?, ?, ?>> cache = new HashMap<>();

    static synchronized Syncer<?, ?, ?> getOrCreateSyncer(Class<?> clazz, Function<Class<?>, Syncer<?, ?, ?>> func) {
        return cache.computeIfAbsent(clazz, func);
    }

    private static Syncer<?, ?, ?> getValueSyncer(Class<?> type) {
        if (type == String.class) {
            return StringSyncer.INSTANCE;
        } else if (type == UUID.class) {
            return UUIDSyncer.INSTANCE;
        } else if (type == ItemStack.class) {
            return ItemStackSyncer.INSTANCE;
        } else if (type == Item.class) {
            return ItemSyncer.INSTANCE;
        } else if (type == Block.class) {
            return BlockSyncer.INSTANCE;
        } else if (type == FluidStack.class) {
            return FluidStackSyncer.INSTANCE;
        } else if (type.isEnum()) {
            return EnumSyncer.get(type);
        } else if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            return getOrCreateSyncer(type, type0 -> {
                Class<?> primitive = Primitives.unwrap(type0);
                String prefix = "de.take_weiland.mods.commons.internal.sync.builtin.";
                String syncerName = StringUtils.capitalize(primitive.getSimpleName());
                if (!type0.isPrimitive()) {
                    syncerName += "Box";
                }
                String postfix = "Syncer";
                try {
                    Class<?> clazz = Class.forName(prefix + syncerName + postfix);
                    return (Syncer<?, ?, ?>) clazz.getEnumConstants()[0];
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            return null;
        }
    }

    private static Syncer<?, ?, ?> getContentsSyncer(Class<?> raw, Property<?, ?> spec) {
        if (FluidTank.class.isAssignableFrom(raw)) {
            if (spec.hasAnnotation(SyncCapacity.class)) {
                return FluidTankSyncer.WithCapacity.INSTANCE;
            } else {
                return FluidTankSyncer.INSTANCE;
            }
        } else {
            return null;
        }
    }

}