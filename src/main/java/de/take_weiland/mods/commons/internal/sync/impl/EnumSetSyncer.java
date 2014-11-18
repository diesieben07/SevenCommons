package de.take_weiland.mods.commons.internal.sync.impl;

import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.InitialDataObject;
import de.take_weiland.mods.commons.sync.ValueSyncer;

import java.lang.reflect.Type;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
// working with raw types here, as this is just not expressible via generics
@SuppressWarnings({"rawtypes", "unchecked"})
public final class EnumSetSyncer implements ValueSyncer<EnumSet>, HandleSubclasses, InitialDataObject.FromGenericType {

	private static final Type iterableType = Iterable.class.getTypeParameters()[0];

	static Class getEnumSetType(Type enumSetType) {
		Class clazz = TypeToken.of(enumSetType).resolveType(iterableType).getRawType();
		if (!clazz.isEnum()) {
			throw new RuntimeException("Could not resolve EnumSet type, got " + clazz);
		}
		return clazz;
	}

	@Override
	public Object initialDataObject(Type type) {
		return new DataObject(getEnumSetType(type));
	}

	@Override
	public boolean hasChanged(EnumSet value, Object data) {
		return !Objects.equal(value, ((DataObject) data).companion);
	}

	@Override
	public Object writeAndUpdate(EnumSet value, MCDataOutputStream out, Object data) {
		out.writeEnumSet(value);
		DataObject dataObject = (DataObject) data;
		if (value == null) {
			dataObject.companion = null;
		} else if (dataObject.companion == null) {
			dataObject.companion = value.clone();
		} else {
			dataObject.companion.clear();
			dataObject.companion.addAll(value);
		}
		return data;
	}

	@Override
	public EnumSet read(MCDataInputStream in, Object data) {
		return in.readEnumSet(((DataObject) data).enumClass);
	}

	public static final class Contents implements ContentSyncer<EnumSet>, InitialDataObject.FromGenericType{

		@Override
		public Object initialDataObject(Type type) {
			return new DataObject(getEnumSetType(type));
		}

		@Override
		public boolean hasChanged(EnumSet value, Object data) {
			return !Objects.equal(value, ((DataObject) data).companion);
		}

		@Override
		public Object writeAndUpdate(EnumSet value, MCDataOutputStream out, Object data) {
			out.writeEnumSet(value);
			DataObject dataObject = (DataObject) data;
			if (dataObject.companion == null) {
				dataObject.companion = value.clone();
			} else {
				dataObject.companion.clear();
				dataObject.companion.addAll(value);
			}
			return data;
		}

		@Override
		public void read(EnumSet value, MCDataInputStream in, Object data) {
			in.readEnumSet(((DataObject) data).enumClass, value);
		}
	}

	private static final class DataObject {

		final Class enumClass;
		EnumSet companion;

		DataObject(Class enumClass) {
			this.enumClass = enumClass;
		}
	}
}
