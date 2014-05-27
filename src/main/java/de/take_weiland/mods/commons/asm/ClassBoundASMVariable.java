package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A class-bound implementation of {@link AbstractASMVariable}
 * @author diesieben07
 */
public abstract class ClassBoundASMVariable extends AbstractASMVariable {

	protected final ClassNode clazz;
	protected final CodePiece instance;

	protected ClassBoundASMVariable(ClassNode clazz, CodePiece instance) {
		this.clazz = checkNotNull(clazz, "clazz");
		this.instance = checkNotNull(instance, "instance");
	}
}
