package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.ValueSyncer;

/**
 * @author diesieben07
 */
@SuppressWarnings("rawtypes")
public class EnumSyncer implements ValueSyncer<Enum>, HandleSubclasses {

	private final Class<? extends Enum> clazz;
	private Enum<?> companion;

	public EnumSyncer(Class<? extends Enum> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean hasChanged(Enum value) {
		return companion != value;
	}

	@Override
	public void writeAndUpdate(Enum value, MCDataOutputStream out) {
		companion = value;
		out.writeEnum(value);
	}

	@Override
	public Enum read(MCDataInputStream in) {
		return in.readEnum(clazz);
	}
}
