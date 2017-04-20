package de.take_weiland.mods.commons.serialize.nbt;

import de.take_weiland.mods.commons.internal.serialize.DummyProperty;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import net.minecraft.nbt.NBTBase;

/**
 * @author diesieben07
 */
final class NBTContentsSerializerShim<T> implements NBTSerializer.Contents<T> {

    private final NBTSerializer<T> delegate;

    NBTContentsSerializerShim(NBTSerializer<T> delegate) {
        if (delegate.characteristics().getSerializationMethod() != SerializationMethod.CONTENTS) {
            throw new UnsupportedOperationException();
        }
        this.delegate = delegate;
    }

    @Override
    public Characteristics characteristics() {
        return delegate.characteristics();
    }

    @Override
    public void read(NBTBase nbt, PropertyAccess<T> property, Object obj) throws SerializationException {
        delegate.read(nbt, property, obj);
    }

    @Override
    public NBTBase write(PropertyAccess<T> property, Object obj) throws SerializationException {
        return delegate.write(property, obj);
    }

    @Override
    public void read(NBTBase nbt, T value) throws SerializationException {
        DummyProperty<T> property = new DummyProperty<>(value);
        delegate.read(nbt, property, null);
    }

    @Override
    public NBTBase write(T value) throws SerializationException {
        DummyProperty<T> property = new DummyProperty<>(value);
        return delegate.write(property, null);
    }

}
