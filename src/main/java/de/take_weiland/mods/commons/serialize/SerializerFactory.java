package de.take_weiland.mods.commons.serialize;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
public interface SerializerFactory {

    @Nullable
    <T> BaseSerializer<T> getSerializer(@Nullable SerializationMethod method, TypeToken<T> type, SerializerRegistry registry);

}
