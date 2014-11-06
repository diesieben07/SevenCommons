package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class ClassBoundASMVariable extends AbstractASMVariable {

	final ClassNode clazz;

	@Nullable
	final CodePiece instance;

	ClassBoundASMVariable(ClassNode clazz, @Nullable CodePiece instance) {
		this.clazz = checkNotNull(clazz, "clazz");
		this.instance = instance;
	}
}
