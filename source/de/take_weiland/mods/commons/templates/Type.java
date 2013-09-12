package de.take_weiland.mods.commons.templates;

public interface Type<T extends Type<T>> extends Stackable, Named {

	// named ordinal for easier Enums
	int ordinal();
	
	Typed<T> getTyped();
	
}
