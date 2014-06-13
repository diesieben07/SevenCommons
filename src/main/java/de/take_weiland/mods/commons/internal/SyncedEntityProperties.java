package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.Entity;

public interface SyncedEntityProperties {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/SyncedEntityProperties";
	public static final String INJECT_DATA = "_sc$injectEntityPropsData";
	public static final String TICK = "_sc$tickEntityProps";
	public static final String GET_ENTITY = "_sc$getPropsEntity";
	public static final String GET_IDENTIFIER = "_sc$getPropsIdentifier";

	void _sc$injectEntityPropsData(Entity owner, String identifier);
	
	void _sc$tickEntityProps();
	
	Entity _sc$getPropsEntity();

	String _sc$getPropsIdentifier();
	
}
