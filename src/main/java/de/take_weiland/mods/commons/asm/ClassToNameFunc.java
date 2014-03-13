package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import org.objectweb.asm.Type;

/**
* @author diesieben07
*/
enum ClassToNameFunc implements Function<Class<?>, String> {
	INSTANCE;

	@Override
	public String apply(Class<?> input) {
		return Type.getInternalName(input);
	}
}
