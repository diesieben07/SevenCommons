package de.take_weiland.mods.commons.nbt;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

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

	@Nonnull
	@Override
	public <OBJ> NBTBase serialize(Property<T, OBJ> property, OBJ instance) {
		return null;
	}

	@Override
	public <OBJ> void deserialize(NBTBase nbt, Property<T, OBJ> property, OBJ instance) {

	}
}
