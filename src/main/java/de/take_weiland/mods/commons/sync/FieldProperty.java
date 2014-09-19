package de.take_weiland.mods.commons.sync;

import com.google.common.base.Throwables;

import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
public class FieldProperty extends Property {

	private final Field field;

	public FieldProperty(Field field) {
		this.field = field;
	}

	@Override
	public void setBoolean(Object instance, boolean value) {
		try {
			field.setBoolean(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setByte(Object instance, byte value) {
		try {
			field.setByte(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setShort(Object instance, short value) {
		try {
			field.setShort(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setChar(Object instance, char value) {
		try {
			field.setChar(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setInt(Object instance, int value) {
		try {
			field.setInt(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setLong(Object instance, long value) {
		try {
			field.setLong(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setFloat(Object instance, float value) {
		try {
			field.setFloat(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void setDouble(Object instance, double value) {
		try {
			field.setDouble(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void set(Object instance, Object value) {
		try {
			field.set(instance, value);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public boolean getBoolean(Object instance) {
		try {
			return field.getBoolean(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public byte getByte(Object instance) {
		try {
			return field.getByte(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public short getShort(Object instance) {
		try {
			return field.getShort(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public char getChar(Object instance) {
		try {
			return field.getChar(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public int getInt(Object instance) {
		try {
			return field.getInt(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public long getLong(Object instance) {
		try {
			return field.getLong(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public float getFloat(Object instance) {
		try {
			return field.getFloat(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public double getDouble(Object instance) {
		try {
			return field.getDouble(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Object get(Object instance) {
		try {
			return field.get(instance);
		} catch (IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}
}
