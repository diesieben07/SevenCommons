package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.util.MultiTypeManager;

interface AbstractItemInternal {

	MultiTypeManager<?> getMultiManager();
	
	void setMultiManager(MultiTypeManager<?> manager);
	
	String getBaseName();
	
	void setBaseName(String baseName);
	
}
