package de.take_weiland.mods.commons.asm;

/**
* @author diesieben07
*/
public class MissingMethodException extends RuntimeException {
	
	private static final long serialVersionUID = -3756768220392690974L;

	MissingMethodException(String text) {
		super(text);
	}

	static MissingMethodException create(String method, String clazz) {
		return new MissingMethodException(method + " in " + clazz);
	}

	static MissingMethodException create(String mcpMethod, String srgMethod, String clazz) {
		return create(formatMcpSrg(mcpMethod, srgMethod), clazz);
	}

	static MissingMethodException withDesc(String method, String desc, String clazz) {
		return create(String.format("%s(%s)", method, desc), clazz);
	}

	static MissingMethodException withDesc(String mcpName, String srgName, String desc, String clazz) {
		return withDesc(formatMcpSrg(mcpName, srgName), desc, clazz);
	}

	private static String formatMcpSrg(String mcp, String srg) {
		return String.format("%s[%s]", mcp, srg);
	}

}
