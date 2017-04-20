package de.take_weiland.mods.commons.serialize.nbt;

import de.take_weiland.mods.commons.internal.serialize.DummyProperty;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import net.minecraft.nbt.NBTBase;

/**
 * @author diesieben07
 */
final class NBTValueSerializerShim<T> implements NBTSerializer.Value<T> {

    private final NBTSerializer<T> delegate;

    NBTValueSerializerShim(NBTSerializer<T> delegate) {
        if (delegate.characteristics().getSerializationMethod() != SerializationMethod.VALUE) {
            throw new UnsupportedOperationException();
        }
        this.delegate = delegate;
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
    public Characteristics characteristics() {
        return delegate.characteristics();
    }

    @Override
    public T read(NBTBase nbt) throws SerializationException {
        DummyProperty<T> property = new DummyProperty<>();
        delegate.read(nbt, property, null);
        return property.value;
    }

    @Override
    public NBTBase write(T value) throws SerializationException {
        DummyProperty<T> property = new DummyProperty<>();
        property.value = value;
        return delegate.write(property, null);
    }
}
