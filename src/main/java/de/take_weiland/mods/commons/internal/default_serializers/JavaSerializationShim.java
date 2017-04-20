package de.take_weiland.mods.commons.internal.default_serializers;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationException;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;

import java.io.*;

/**
 * @author diesieben07
 */
final class JavaSerializationShim<T extends Serializable> implements NBTSerializer.Value<T> {

    private static final Characteristics C = new Characteristics(SerializationMethod.VALUE, true, NBT.Tag.BYTE_ARRAY);

    private final Class<? super T> rawType;

    JavaSerializationShim(TypeToken<T> type) {
        rawType = type.getRawType();
    }

    @Override
    public Characteristics characteristics() {
        return C;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(NBTBase nbt) throws SerializationException {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(((NBTTagByteArray) nbt).getByteArray());
             ObjectInputStream in = new ObjectInputStream(bytes)) {
            Object o = in.readObject();
            if (!rawType.isInstance(o)) {
                throw new SerializationException("Unexpected class " + o.getClass().getName() + ", expected " + rawType.getName());
            }
            return (T) o;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public NBTBase write(T value) throws SerializationException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
            return new NBTTagByteArray(bytes.toByteArray());
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
