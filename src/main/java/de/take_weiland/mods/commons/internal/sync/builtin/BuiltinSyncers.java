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

/**
 * @author diesieben07
 */
public final class BuiltinSyncers implements SyncerFactory {

    private final Map<Class<?>, Syncer<?, ?, ?>> cache = new HashMap<>();

    @Override
    public <T_VAL> Syncer<T_VAL, ?, ?> getSyncer(Property<T_VAL, ?> type) {
        Class<? super T_VAL> raw = type.getRawType();

        Syncer<?, ?, ?> syncer;
        if (type.getDesiredMethod() != SerializationMethod.Method.CONTENTS) {
            syncer = cache.get(raw);
            if (syncer == null && !cache.containsKey(raw)) {
                syncer = newSyncerForRawType(raw);
                cache.put(raw, syncer);
            }
        } else {
            syncer = null;
        }

        if (syncer == null && type.getDesiredMethod() != SerializationMethod.Method.VALUE) {
            syncer = getSpecialSyncer(raw, type);
        }
        //noinspection unchecked
        return (Syncer<T_VAL, ?, ?>) syncer;
    }

    private static Syncer<?, ?, ?> newSyncerForRawType(Class<?> type) {
        if (type == String.class) {
            return StringSyncer.INSTANCE;
        } else if (type == UUID.class) {
            return UUIDSyncer.INSTANCE;
        } else if (type == ItemStack.class) {
            return ItemStackSyncer.INSTANCE;
        } else if (type == Item.class) {
            return ItemStackSyncer.INSTANCE;
        } else if (type == Block.class) {
            return BlockSyncer.INSTANCE;
        } else if (type == FluidStack.class) {
            return FluidStackSyncer.INSTANCE;
        } else if (type.isEnum()) {
            //noinspection unchecked
            return new EnumSyncer(type);
        } else if (type.isPrimitive() || Primitives.isWrapperType(type)){
            String className = StringUtils.capitalize(Primitives.unwrap(type).getSimpleName());
            if (!type.isPrimitive()) {
                className += "Box";
            }
            className += "Syncer";
            Class<?> clazz;
            try {
                clazz = Class.forName("de.take_weiland.mods.commons.internal.sync.builtin." + className);
                return (Syncer<?, ?, ?>) clazz.getEnumConstants()[0];
            } catch (ClassNotFoundException e) {
                throw new AssertionError("Missing primitive type " + type);
            }
        } else {
            return null;
        }
    }

    private static Syncer<?, ?, ?> getSpecialSyncer(Class<?> raw, Property<?, ?> spec) {
        if (FluidTank.class.isAssignableFrom(raw)) {
            if (spec.hasAnnotation(SyncCapacity.class)) {
                return FluidTankAndCapacitySyncer.INSTANCE;
            } else {
                return FluidTankSyncer.INSTANCE;
            }
        } else {
            return null;
        }
    }

}