package de.take_weiland.mods.commons.asm.info;

/**
 * @author diesieben07
 */
public abstract class AnnotationInfo {

	public abstract boolean hasProperty(String prop);

	public <T> T getProperty(String prop) {
		return getProperty(prop, null);
	}

	public abstract <T> T getProperty(String prop, T defaultValue);

}
