package de.take_weiland.mods.commons.sync;

import com.google.common.base.Throwables;

import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
public class GetterSetterProperty extends Property {

	private final Method getter;
	private final Method setter;

	public GetterSetterProperty(Method getter, Method setter) {
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public void setBoolean(Object instance, boolean value) {
		set(instance, value);
	}

	@Override
	public void setByte(Object instance, byte value) {
		set(instance, value);
	}

	@Override
	public void setShort(Object instance, short value) {
		set(instance, value);
	}

	@Override
	public void setChar(Object instance, char value) {
		set(instance, value);
	}

	@Override
	public void setInt(Object instance, int value) {
		set(instance, value);
	}

	@Override
	public void setLong(Object instance, long value) {
		set(instance, value);
	}

	@Override
	public void setFloat(Object instance, float value) {
		set(instance, value);
	}

	@Override
	public void setDouble(Object instance, double value) {
		set(instance, value);
	}

	@Override
	public void set(Object instance, Object value) {
		try {
			setter.invoke(instance, value);
		} catch (ReflectiveOperationException e) {
			Throwables.propagate(e);
		}
	}

	@Override
	public boolean getBoolean(Object instance) {
		return (boolean) get(instance);
	}

	@Override
	public byte getByte(Object instance) {
		return (byte) get(instance);
	}

	@Override
	public short getShort(Object instance) {
		return (short) get(instance);
	}

	@Override
	public char getChar(Object instance) {
		return (char) get(instance);
	}

	@Override
	public int getInt(Object instance) {
		return (int) get(instance);
	}

	@Override
	public long getLong(Object instance) {
		return (long) get(instance);
	}

	@Override
	public float getFloat(Object instance) {
		return (float) get(instance);
	}

	@Override
	public double getDouble(Object instance) {
		return (double) get(instance);
	}

	@Override
	public Object get(Object instance) {
		try {
			return getter.invoke(instance);
		} catch (ReflectiveOperationException e) {
			throw Throwables.propagate(e);
		}
	}
}
