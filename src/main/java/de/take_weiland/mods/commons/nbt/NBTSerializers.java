package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.internal.tonbt.ToNbtFactories;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Registry for NBT serializers to be used with {@link de.take_weiland.mods.commons.nbt.ToNbt @ToNbt}.</p>
 *
 * @author diesieben07
 * @see de.take_weiland.mods.commons.nbt.NBTSerializerFactory
 */
@ParametersAreNonnullByDefault
public final class NBTSerializers {

    /**
     * <p>Register a {@code NBTSerializerFactory}. The factory will be called back for all properties whose type
     * extends or implements the given {@code baseClass}.If {@code baseClass} is {@code Object}, the factory will also
     * receive callbacks for primitive types.</p>
     * @param baseClass the base class
     * @param factory the factory
     */
    public static void register(Class<?> baseClass, NBTSerializerFactory factory) {
        ToNbtFactories.registerFactory(baseClass, factory);
    }

}
