package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.reflect.Getter;
import de.take_weiland.mods.commons.reflect.Invoke;
import de.take_weiland.mods.commons.reflect.SCReflection;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;

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
		return LocaleAcc.instance.translate(LocaleAcc.instance.getCurrentLocale(), key);
	}

	/**
	 * <p>Translate the given key and pass the result to {@link String#format(String, Object...)} with the given args.</p>
	 * @param key the key to translate
	 * @param args the formatting arguments
	 * @return the translation
	 */
	public static String translate(String key, Object... args) {
		return LocaleAcc.instance.getCurrentLocale().formatMessage(key, args);
	}

    private interface LocaleAcc {

        LocaleAcc instance = SCReflection.createAccessor(LocaleAcc.class);

        @Invoke(method = MCPNames.M_TRANSLATE_KEY_PRIVATE, srg = true)
        String translate(Locale instance, String key);

        @Getter(target = LanguageManager.class, field = MCPNames.F_CURRENT_LOCALE, srg = true)
        Locale getCurrentLocale();

    }

	private I18n() { }
}
