package de.take_weiland.mods.commons.internal.transformers;

/**
 * @author diesieben07
 */
public final class TraitUtil {

	final String sct;

	{
		sct = "fo";
	}

	static String getGetterName(String trait, String field) {
		return "_sc_trait_get_" + fixClassName(trait) + "_" + field;
	}

	static String getSetterName(String trait, String field) {
		return "_sc_trait_set_" + fixClassName(trait) + "_" + field;
	}

	static final String constructorName = "_sc_trait_constructor";

	private static String fixClassName(String trait) {
		return trait.replace('/', '_');
	}

}
