package de.take_weiland.mods.commons.fastreflect;

public interface AccessorFactory {

	<T> T createAccessor(Class<T> iface);
	
}
