package de.take_weiland.mods.commons.internal;

/**
 * @author diesieben07
 */
public interface SimpleMetadataProxy {

	public static final String SETTER = "_sc$injectMetadataHolder";
	public static final String GETTER = "_sc$getMetadataHolder";

	void _sc$injectMetadataHolder(Object holder);

	Object _sc$getMetadataHolder();

}
