package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

/**
 * @author diesieben07
 */
public interface SyncTypeInfo {

	Class<?> getRawType();

	TypeToken<?> getGenericType();

}
