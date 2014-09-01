package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;

/**
 * @author diesieben07
 */
public abstract class AbstractAnalyzingTransformer implements ASMClassTransformer {

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/")
				&& !internalName.startsWith("org/apache/");
	}
}
