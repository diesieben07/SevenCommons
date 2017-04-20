package de.take_weiland.mods.commons.serialize.bytes;

import de.take_weiland.mods.commons.internal.serialize.DummyProperty;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;

/**
 * @author diesieben07
 */
final class ByteValueSerializerShim<T> implements ByteStreamSerializer.Value<T> {

    private final ByteStreamSerializer<T> delegate;

    ByteValueSerializerShim(ByteStreamSerializer<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Characteristics characteristics() {
        return delegate.characteristics();
    }

    @Override
    public T read(MCDataInput in) {
        DummyProperty<T> property = new DummyProperty<>();
        delegate.read(in, property, null);
        return property.value;
    }

    @Override
    public void write(MCDataOutput out, T instance) {
        DummyProperty<T> property = new DummyProperty<>(instance);
        delegate.write(out, property, null);
    }

    @Override
    public void write(MCDataOutput out, PropertyAccess<T> property, Object obj) {
        delegate.write(out, property, obj);
    }

    @Override
    public void read(MCDataInput in, PropertyAccess<T> property, Object obj) {
        delegate.read(in, property, obj);
    }
}
