package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.Entity;

public interface SyncedEntityProperties {

	public static final String INJECT_DATA = "_sc$injectEntityPropsData";
	public static final String TICK = "_sc$tickEntityProps";
	public static final String GET_ENTITY = "_sc$getPropsEntity";
	public static final String GET_INDEX = "_sc$getPropsIndex";
	public static final String GET_IDENTIFIER = "_sc$getPropsIdentifier";

	void _sc$injectEntityPropsData(Entity owner, String identifier, int idx);
	
	void _sc$tickEntityProps();
	
	Entity _sc$getPropsEntity();
	
	int _sc$getPropsIndex();
	
	String _sc$getPropsIdentifier();
	
}
