package de.take_weiland.mods.commons.templates;

public interface Typed<E extends Type<E>> extends Named, Stackable {

	public E[] getTypes();
	
	public E getDefault();
	
}
