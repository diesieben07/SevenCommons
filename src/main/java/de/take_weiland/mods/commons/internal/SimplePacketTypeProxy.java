package de.take_weiland.mods.commons.internal;

/**
 * @author diesieben07
 */
public interface SimplePacketTypeProxy {

	public static final String GETTER = "_sc$getPacketFactory";
	public static final String SETTER = "_sc$setPacketFactory";

	Object _sc$getPacketFactory();

	void _sc$setPacketFactory(Object factory);

}
