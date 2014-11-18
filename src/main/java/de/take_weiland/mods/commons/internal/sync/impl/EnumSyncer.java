package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.InitialDataObject;
import de.take_weiland.mods.commons.sync.ValueSyncer;

/**
 * @author diesieben07
 */
@SuppressWarnings("rawtypes")
public class EnumSyncer implements ValueSyncer<Enum>, HandleSubclasses, InitialDataObject.FromType {

	@Override
	public Object initialDataObject(Class type) {
		return new DataObject(type);
	}

	@Override
	public boolean hasChanged(Enum value, Object data) {
		return ((DataObject) data).companion != value;
	}

	@Override
	public Object writeAndUpdate(Enum value, MCDataOutputStream out, Object data) {
		((DataObject) data).companion = value;
		out.writeEnum(value);
		return data;
	}

	@Override
	public Enum read(MCDataInputStream in, Object data) {
		return in.readEnum(((DataObject) data).clazz);
	}

	private static final class DataObject {

		private final Class clazz;
		private Enum companion;

		DataObject(Class clazz) {
			this.clazz = clazz;
		}
	}
}
