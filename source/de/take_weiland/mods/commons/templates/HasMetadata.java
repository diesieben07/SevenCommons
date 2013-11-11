package de.take_weiland.mods.commons.templates;

public interface HasMetadata<TYPE extends Metadata> {

	TYPE[] getTypes();
	
	TYPE getDefault();
	
}
