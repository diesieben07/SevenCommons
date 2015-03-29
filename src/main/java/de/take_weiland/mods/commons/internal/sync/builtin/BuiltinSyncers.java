package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.sync.SyncCapacity;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author diesieben07
 */
public final class BuiltinSyncers implements SyncerFactory {

    private final Map<Class<?>, Syncer<?, ?>> cache = new HashMap<>();

    @Override
    public <V, C> Syncer<V, C> getSyncer(TypeSpecification<V> type) {
        Class<? super V> raw = type.getRawType();

        Syncer<?, ?> syncer;
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
        return (Syncer<V, C>) syncer;
    }

    private static Syncer<?, ?> newSyncerForRawType(Class<?> type) {
        if (type == String.class) {
            return new StringSyncer();
        } else if (type == UUID.class) {
            return new UUIDSyncer();
        } else if (type == ItemStack.class) {
            return new ItemStackSyncer();
        } else if (type == Item.class) {
            return new ItemSyncer();
        } else if (type == Block.class) {
            return new BlockSyncer();
        } else if (type == FluidStack.class) {
            return new FluidStackSyncer();
        } else if (type.isEnum()) {
            //noinspection unchecked
            return new EnumSyncer(type);
        } else {
            return PrimitiveAndBoxSyncerFactory.createSyncer(type);
        }
    }

    private static Syncer<?, ?> getSpecialSyncer(Class<?> raw, TypeSpecification<?> spec) {
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