package de.take_weiland.mods.commons.internal.default_serializers;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.bytes.ByteStreamSerializer;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;

/**
 * @author diesieben07
 */
abstract class ByteStreamAsNBTSerializer<T, SER extends ByteStreamSerializer<T>> implements NBTSerializer<T> {

    static <T> NBTSerializer<T> create(ByteStreamSerializer<T> delegate) {
        if (delegate.characteristics().getSerializationMethod() == SerializationMethod.VALUE) {
            return new Value<>(delegate.asValueSerializer());
        } else  {
            return new Contents<>(delegate.asContentsSerializer());
        }
    }

    final SER delegate;

    ByteStreamAsNBTSerializer(SER delegate) {
        this.delegate = delegate;
    }

    @Override
    public Characteristics characteristics() {
        return null;
    }

    @Override
    public void read(NBTBase nbt, PropertyAccess<T> property, Object obj) throws SerializationException {
        byte[] bytes = ((NBTTagByteArray) nbt).getByteArray();
        delegate.read(Network.newInput(Unpooled.wrappedBuffer(bytes)), property, obj);
    }

    @Override
    public NBTBase write(PropertyAccess<T> property, Object obj) throws SerializationException {
        MCDataOutput out = Network.newOutput();
        delegate.write(out, property, obj);
        return new NBTTagByteArray(out.toByteArray());
    }

    private static final class Value<T> extends ByteStreamAsNBTSerializer<T, ByteStreamSerializer.Value<T>> implements NBTSerializer.Value<T> {

        Value(ByteStreamSerializer.Value<T> delegate) {
            super(delegate);
        }

        @Override
        public T read(NBTBase nbt) throws SerializationException {
            byte[] bytes = ((NBTTagByteArray) nbt).getByteArray();
            return delegate.read(Network.newInput(Unpooled.wrappedBuffer(bytes)));
        }

        @Override
        public NBTBase write(T value) throws SerializationException {
            MCDataOutput out = Network.newOutput();
            delegate.write(out, value);
            return new NBTTagByteArray(out.toByteArray());
        }
    }

    private static final class Contents<T> extends ByteStreamAsNBTSerializer<T, ByteStreamSerializer.Contents<T>> implements NBTSerializer.Contents<T> {

        Contents(ByteStreamSerializer.Contents<T> delegate) {
            super(delegate);
        }

        @Override
        public void read(NBTBase nbt, T value) throws SerializationException {
            byte[] bytes = ((NBTTagByteArray) nbt).getByteArray();
            delegate.read(Network.newInput(Unpooled.wrappedBuffer(bytes)), value);
        }

        @Override
        public NBTBase write(T value) throws SerializationException {
            MCDataOutput out = Network.newOutput();
            delegate.write(out, value);
            return new NBTTagByteArray(out.toByteArray());
        }
    }

}
