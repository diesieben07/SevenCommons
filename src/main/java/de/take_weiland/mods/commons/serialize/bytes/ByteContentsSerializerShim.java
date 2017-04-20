package de.take_weiland.mods.commons.serialize.bytes;

import de.take_weiland.mods.commons.internal.serialize.DummyProperty;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;

/**
 * @author diesieben07
 */
final class ByteContentsSerializerShim<T> implements ByteStreamSerializer.Contents<T> {

    private final ByteStreamSerializer<T> delegate;

    ByteContentsSerializerShim(ByteStreamSerializer<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Characteristics characteristics() {
        return delegate.characteristics();
    }

    @Override
    public void read(MCDataInput in, T instance) {
        DummyProperty<T> property = new DummyProperty<>(instance);
        delegate.read(in, property, null);
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
