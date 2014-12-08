package de.take_weiland.mods.commons.properties;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public final class Types {

	private static BiMap<Class<?>, String> typeIDs = HashBiMap.create();

	public static Class<?> getClass(String id) {
		return typeIDs.inverse().get(id);
	}

	public static String getID(Class<?> clazz) {
		return typeIDs.get(clazz);
	}

	public static void setID(Class<?> clazz, String id) {
		checkState(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION), "Register TypeIDs before postInit");
		if (typeIDs.containsKey(clazz) || typeIDs.containsValue(id)) {
			throw new IllegalArgumentException(String.format("Duplicate type mapping %s<>%s", clazz.getName(), id));
		}
		typeIDs.put(clazz, id);
	}

	static void freeze() {
		typeIDs = ImmutableBiMap.copyOf(typeIDs);
	}

	private Types() { }

}
