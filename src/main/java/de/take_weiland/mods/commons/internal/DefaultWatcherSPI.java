package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.internal.syncimpl.*;
import de.take_weiland.mods.commons.sync.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
final class DefaultWatcherSPI implements WatcherSPI {

	private static final Type enumSetType = EnumSet.class.getTypeParameters()[0];

	@SuppressWarnings({"rawtypes", "unchecked"}) // we make sure things line up
	@Override
	public Watcher provideWatcher(PropertyMetadata propertyMetadata, Sync.Method method) {
		Class<?> rawType = propertyMetadata.getRawType();

		if (rawType == ItemStack.class) {
			return method == Sync.Method.CONTENTS ? ItemStackWatcher.CONTENTS : ItemStackWatcher.VALUE;
		}

		if (rawType == FluidStack.class) {
			return method == Sync.Method.CONTENTS ? FluidStackWatcher.CONTENTS : FluidStackWatcher.VALUE;
		}

		if (rawType == BitSet.class) {
			return method == Sync.Method.CONTENTS ? BitSetWatcher.CONTENTS : BitSetWatcher.VALUE;
		}

		if (rawType == UUID.class && method != Sync.Method.CONTENTS) {
			return UUIDWatcher.INSTANCE;
		}

		if (rawType == String.class && method != Sync.Method.CONTENTS) {
			return StringWatcher.INSTANCE;
		}

		if (rawType == EnumSet.class) {
			Class<?> enumType = propertyMetadata.getType().resolveType(enumSetType).getRawType();
			if (!enumType.isEnum()) {
				return null;
			} else {
				if (method == Sync.Method.CONTENTS) {
					return EnumSetWatcher.getContentsWatcher(rawType);
				} else {
					return EnumSetWatcher.getValueWatcher(rawType);
				}
			}
		}

		if (Block.class.isAssignableFrom(rawType) && method != Sync.Method.CONTENTS) {
			return BlockWatcher.INSTANCE;
		}

		if (Item.class.isAssignableFrom(rawType) && method != Sync.Method.CONTENTS) {
			return ItemWatcher.INSTANCE;
		}

		if (FluidTank.class.isAssignableFrom(rawType)) {
			if (propertyMetadata.isAnnotationPresent(SyncCapacity.class)) {
				return FluidTankWatcher.WithCapacity.INSTANCE;
			} else {
				return FluidTankWatcher.INSTANCE;
			}
		}


		if (method != Sync.Method.CONTENTS && rawType.isEnum()) {
			return EnumWatcher.get(rawType);
		}

		return null;
	}
}
