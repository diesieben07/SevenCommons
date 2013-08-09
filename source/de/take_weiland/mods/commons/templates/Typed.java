package de.take_weiland.mods.commons.templates;

public interface Typed<E extends Type> {

	public E[] getTypes();
	
	public E getDefault();
	
}
