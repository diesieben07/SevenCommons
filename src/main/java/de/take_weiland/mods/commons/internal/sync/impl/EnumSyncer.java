package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.SyncElement;
import de.take_weiland.mods.commons.sync.SyncerProvider;
import de.take_weiland.mods.commons.sync.ValueSyncer;

import static de.take_weiland.mods.commons.internal.sync.SyncingManager.sync;

/**
 * @author diesieben07
 */
public final class EnumSyncer<E extends Enum<E>> implements ValueSyncer<E> {

	public static void register() {
		sync(Enum.class)
				.andSubclasses()
				.with(new SyncerProvider.ForValue() {
					@SuppressWarnings({"unchecked", "rawtypes"})
					@Override
					public <S> ValueSyncer<S> apply(SyncElement<S> element) {
						Class rawType = element.getType().getRawType();
						if (rawType.isEnum()) {
							return new EnumSyncer(rawType);
						} else {
							return null;
						}
					}
				});
	}

	private final Class<E> clazz;

	EnumSyncer(Class<E> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean hasChanged(E value, Object data) {
		return value != data;
	}

	@Override
	public Object writeAndUpdate(E value, MCDataOutputStream out, Object data) {
		out.writeEnum(value);
		return value;
	}

	@Override
	public E read(MCDataInputStream in, Object data) {
		return in.readEnum(clazz);
	}

}
