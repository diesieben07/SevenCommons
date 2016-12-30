package de.take_weiland.mods.commons.serialize;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

/**
 * @author diesieben07
 */
public interface SerializationTarget {

    Class<? extends BaseSerializer> getInterface(SerializationMethod method);

    default <T> TypeToken<? extends BaseSerializer<T>> getTypedInterface(SerializationMethod method, TypeToken<T> type) {
        return new TypeToken<BaseSerializer<T>>() {}.where(new TypeParameter<T>() {}, type)
                .getSubtype(getInterface(method));
    }

    SerializationTarget BYTE_STREAM = method -> {
        switch (method) {
            case DEFAULT:
                return ByteStreamSerializer.class;
            case CONTENTS:
                return ByteStreamSerializer.Contents.class;
            case VALUE:
                return ByteStreamSerializer.Instance.class;
            default:
                throw new RuntimeException();
        }
    };

    SerializationTarget NBT = method -> {
        switch (method) {
            case DEFAULT:
                return NBTSerializer.class;
            case CONTENTS:
                return NBTSerializer.Contents.class;
            case VALUE:
                return NBTSerializer.Instance.class;
            default:
                throw new RuntimeException();
        }
    };

}
