package de.take_weiland.mods.commons.asm.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark this method of a ProxyInterface as being a getter<br>
 * The method should have a return type that matches the specified field and accept no parameters.
 * @author diesieben07
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Getter {

	/**
	 * the name of the field to get, when in an MCP-Environment
	 * @return the MCP name (e.g. <tt>theEntity</tt>)
	 */
	public String mcpName();
	
	/**
	 * the name of the field to get, when in an obfuscated environment
	 * @return the obfuscated name (e.g. <tt>a</tt>)
	 */
	public String obfName();
	
}
