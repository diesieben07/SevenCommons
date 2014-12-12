package de.take_weiland.mods.commons.properties;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.PacketTypeIds;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Types {

	private static BiMap<String, Class<?>> typeIDs = HashBiMap.create();
	private static BiMap<Integer, Class<?>> numericalIDs;

	public static void setID(Class<?> clazz, String id) {
		checkState(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION), "Register TypeIDs before postInit");
		if (typeIDs.containsKey(id) || typeIDs.containsValue(clazz)) {
			throw new IllegalArgumentException(String.format("Duplicate type mapping %s<>%s", clazz.getName(), id));
		}
		typeIDs.put(id, clazz);
	}

	public static Class<?> getClass(String id) {
		Class<?> clazz = typeIDs.get(id);
		if (clazz == null) {
			throw new IllegalArgumentException("No such TypeID");
		}
		return clazz;
	}

	public static Class<?> getClass(int id) {
		Class<?> clazz = numericalIDs.get(id);
		if (clazz == null) {
			throw new IllegalArgumentException("Unknown numerical TypeID");
		}
		return clazz;
	}

	public static Optional<Class<?>> getClassOptional(String id) {
		// weird type inference
		return Optional.<Class<?>>fromNullable(typeIDs.get(id));
	}

	public static Optional<Class<?>> getClassOptional(int id) {
		return Optional.<Class<?>>fromNullable(numericalIDs.get(id));
	}

	public static String getID(Class<?> clazz) {
		String id = typeIDs.inverse().get(clazz);
		if (id == null) {
			throw noID(clazz);
		}
		return id;
	}

	public static int getNumericalID(Class<?> clazz) {
		Integer id = numericalIDs.inverse().get(clazz);
		if (id == null) {
			throw noID(clazz);
		}
		return numericalIDs.inverse().get(clazz);
	}

	public static Optional<String> getIDOptional(Class<?> clazz) {
		return Optional.fromNullable(typeIDs.inverse().get(clazz));
	}

	public static Optional<Integer> getNumericalIDOptional(Class<?> clazz) {
		return Optional.fromNullable(numericalIDs.inverse().get(clazz));
	}

	private static RuntimeException noID(Class<?> clazz) {
		throw new RuntimeException("No TypeID registered for class " + clazz.getName());
	}

	public static void freeze() {
		typeIDs = ImmutableBiMap.copyOf(typeIDs);
		ImmutableBiMap.Builder<Integer, Class<?>> builder = ImmutableBiMap.builder();

		int i = 0;
		for (Class<?> clazz : typeIDs.values()) {
			builder.put(i++, clazz);
		}
		numericalIDs = builder.build();
	}

	public static void injectNumericalIDs(Map<Integer, Class<?>> map) {
		numericalIDs = ImmutableBiMap.copyOf(map);
	}

	public static void sendTypeIDs(EntityPlayer player) {
		new PacketTypeIds(numericalIDs).sendTo(player);
	}

	private Types() { }
}
