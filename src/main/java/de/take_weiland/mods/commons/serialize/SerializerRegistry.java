package de.take_weiland.mods.commons.serialize;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.serialize.bytes.ByteStreamSerializer;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author diesieben07
 */
public final class SerializerRegistry {

    private final List<NBTSerializer.Factory> nbtFactories = new ArrayList<>();
    private final List<ByteStreamSerializer.Factory> byteFactories = new ArrayList<>();

    public <T> NBTSerializer<T> getNbtSerializer(@Nullable SerializationMethod method, TypeToken<T> type) {
        return (NBTSerializer<T>) getSerializer(method, type, nbtFactories);
    }

    public <T> ByteStreamSerializer<T> getByteStreamSerializer(@Nullable SerializationMethod method, TypeToken<T> type) {
        return (ByteStreamSerializer<T>) getSerializer(method, type, byteFactories);
    }

   private <T> BaseSerializer<T> getSerializer(SerializationMethod method, TypeToken<T> type, List<? extends SerializerFactory> factories) {
        return factories.stream()
                .map(f -> f.getSerializer(method, type, this))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
