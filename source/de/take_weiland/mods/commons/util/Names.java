package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.templates.Named;

public final class Names {

	private Names() { }
	
	public static String combine(Named name1, Named name2) {
		return combine(name1.unlocalizedName(), name2.unlocalizedName());
	}
	
	public static String combine(Named name1, String name2) {
		return name1.unlocalizedName() + "." + name2;
	}
	
	public static String combine(String name1, Named name2) {
		return name1 + "." + name2.unlocalizedName();
	}
	
	public static String combine(String name1, String name2) {
		return name1 + "." + name2;
	}

}
