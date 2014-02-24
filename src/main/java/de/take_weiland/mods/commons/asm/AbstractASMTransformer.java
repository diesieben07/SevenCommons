package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.ClassWriter;

/**
 * An abstract class for {@link de.take_weiland.mods.commons.asm.ASMClassTransformer ASMClassTransformers}. Defines default values for
 * {@link #getClassReaderFlags()} and {@link #getClassWriterFlags()}
 *
 * @author diesieben07
 */
public abstract class AbstractASMTransformer implements ASMClassTransformer {

	@Override
	public int getClassWriterFlags() {
		return ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;
	}

	@Override
	public int getClassReaderFlags() {
		return 0;
	}
}
