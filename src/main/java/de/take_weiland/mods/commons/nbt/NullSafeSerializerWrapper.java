package de.take_weiland.mods.commons.nbt;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class NullSafeSerializerWrapper<T> implements NBTSerializer<T> {

	private static final LoadingCache<NBTSerializer<?>, NBTSerializer<?>> cache = CacheBuilder.newBuilder()
			.concurrencyLevel(2)
			.weakKeys()
			.build(new CacheLoader<NBTSerializer<?>, NBTSerializer<?>>() {
				@Override
				public NBTSerializer<?> load(@NotNull NBTSerializer<?> key) throws Exception {
					return new NullSafeSerializerWrapper<>(key);
				}
			});

	@SuppressWarnings("unchecked")
	static <T> NBTSerializer<T> makeNullSafe(NBTSerializer<T> serializer) {
		return (NBTSerializer<T>) cache.getUnchecked(serializer);
	}

	private final NBTSerializer<T> wrapped;

	NullSafeSerializerWrapper(NBTSerializer<T> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public NBTBase serialize(@Nullable T instance) {
		return instance == null ? NBTData.serializedNull() : wrapped.serialize(instance);
	}

	@Override
	public T deserialize(@Nullable NBTBase nbt) {
		return NBTData.isSerializedNull(nbt) ? null : wrapped.deserialize(nbt);
	}
}
