package de.take_weiland.mods.commons.nbt;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class EnumSerializer<E extends Enum<E>> implements NBTSerializer<E> {

	private static final LoadingCache<Class<? extends Enum<?>>, EnumSerializer<?>> cache = CacheBuilder.newBuilder()
			.concurrencyLevel(2)
			.build(new CacheLoader<Class<? extends Enum<?>>, EnumSerializer<?>>() {
				@SuppressWarnings({"unchecked", "rawtypes"})
				@Override
				public EnumSerializer<?> load(@Nonnull Class<? extends Enum<?>> key) throws Exception {
					return new EnumSerializer(key);
				}
			});

	@SuppressWarnings("unchecked")
	@NBTSerializer.Provider(method = SerializationMethod.VALUE)
	public static Object provider(TypeSpecification<?> type) {
		Class<?> rawType = type.getRawType();
		if (rawType.isEnum()) {
			return cache.getUnchecked((Class<? extends Enum<?>>) rawType);
		} else {
			return null;
		}
	}

	private final Class<E> clazz;

	EnumSerializer(Class<E> clazz) {
		this.clazz = clazz;
	}

	@Nonnull
	@Override
	public NBTBase serialize(@Nullable E instance) {
		return NBTData.writeEnum(instance);
	}

	@Override
	public E deserialize(@Nullable NBTBase nbt) {
		return NBTData.readEnum(nbt, clazz);
	}
}
