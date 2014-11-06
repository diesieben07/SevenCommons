package de.take_weiland.mods.commons.client;

/**
 * <p>Utility class for translating.</p>
 *
 * @author diesieben07
 */
public final class I18n {

	/**
	 * <p>Translate the given key.</p>
	 * @param key the key to translate
	 * @return the translation
	 */
	public static String translate(String key) {
		return net.minecraft.client.resources.I18n.getString(key);
	}

	/**
	 * <p>Translate the given key and pass the result to {@link String#format(String, Object...)} with the given args.</p>
	 * @param key the key to translate
	 * @param args the formatting arguments
	 * @return the translation
	 */
	public static String translate(String key, Object... args) {
		return String.format(net.minecraft.client.resources.I18n.getString(key), args);
	}

	private I18n() { }
}
