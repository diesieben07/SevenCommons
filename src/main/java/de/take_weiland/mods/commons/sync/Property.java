package de.take_weiland.mods.commons.sync;

/**
 * @author diesieben07
 */
public abstract class Property {

	public abstract void setBoolean(Object instance, boolean value);

	public abstract void setByte(Object instance, byte value);

	public abstract void setShort(Object instance, short value);

	public abstract void setChar(Object instance, char value);

	public abstract void setInt(Object instance, int value);

	public abstract void setLong(Object instance, long value);

	public abstract void setFloat(Object instance, float value);

	public abstract void setDouble(Object instance, double value);

	public abstract void set(Object instance, Object value);

	public abstract boolean getBoolean(Object instance);

	public abstract byte getByte(Object instance);

	public abstract short getShort(Object instance);

	public abstract char getChar(Object instance);

	public abstract int getInt(Object instance);

	public abstract long getLong(Object instance);

	public abstract float getFloat(Object instance);

	public abstract double getDouble(Object instance);

	public abstract Object get(Object instance);
}
