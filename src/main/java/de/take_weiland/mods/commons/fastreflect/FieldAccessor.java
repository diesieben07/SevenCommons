package de.take_weiland.mods.commons.fastreflect;

public interface FieldAccessor {

	boolean getBoolean(Object obj);
	byte getByte(Object obj);
	short getShort(Object obj);
	int getInt(Object obj);
	long getLong(Object obj);
	char getChar(Object obj);
	float getFloat(Object obj);
	double getDouble(Object obj);
	
	void setBoolean(Object obj, boolean val);
	void setByte(Object obj, byte val);
	void setShort(Object obj, short val);
	void setInt(Object obj, int val);
	void setLong(Object obj, long val);
	void setChar(Object obj, char val);
	void setFloat(Object obj, float val);
	void setDouble(Object obj, double val);
	
}
