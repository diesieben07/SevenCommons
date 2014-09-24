package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * @author diesieben07
 */
public abstract class PropertyWatcher<T> {

	PropertyWatcher() { }

	public abstract boolean hasChanged(T value);

	public abstract void writeAndUpdate(T value, MCDataOutputStream out);

	public abstract <T0 extends T> T0 readBase(T0 value, MCDataInputStream in);

	public static abstract class WithSubclasses<T> extends PropertyWatcher<T> {

		public WithSubclasses() { }

		@Override
		public <T0 extends T> T0 readBase(T0 value, MCDataInputStream in) {
			readInPlace(value, in);
			return value;
		}

		public abstract void readInPlace(T value, MCDataInputStream in);
	}

	public static abstract class Standard<T> extends PropertyWatcher<T> {

		public Standard() { }

		@SuppressWarnings("unchecked")
		@Override
		public final <T0 extends T> T0 readBase(T0 value, MCDataInputStream in) {
			return (T0) read(value, in);
		}

		public abstract T read(T value, MCDataInputStream in);
	}

}