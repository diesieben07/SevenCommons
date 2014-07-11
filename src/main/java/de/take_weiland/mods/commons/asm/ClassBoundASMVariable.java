package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class ClassBoundASMVariable extends AbstractASMVariable {

	final ClassNode clazz;
	final CodePiece instance;

	ClassBoundASMVariable(ClassNode clazz, CodePiece instance) {
		this.clazz = checkNotNull(clazz, "clazz");
		this.instance = instance;
	}
}
