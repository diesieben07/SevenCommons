package de.take_weiland.mods.commons.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.SevenCommons;

/**
 * @author diesieben07
 */
@SideOnly(Side.CLIENT)
public final class I18n {

	public static String translate(String key) {
		return SevenCommons.proxy.translate(key);
	}

	public static String translate(String key, Object... args) {
		return String.format(SevenCommons.proxy.translate(key), args);
	}

	private I18n() { }
}
