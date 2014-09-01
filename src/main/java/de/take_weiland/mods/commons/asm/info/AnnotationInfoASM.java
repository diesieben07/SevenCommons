package de.take_weiland.mods.commons.asm.info;

import de.take_weiland.mods.commons.asm.ASMUtils;
import org.objectweb.asm.tree.AnnotationNode;

/**
 * @author diesieben07
 */
class AnnotationInfoASM extends AnnotationInfo {

	private final AnnotationNode annotation;

	AnnotationInfoASM(AnnotationNode annotation) {
		this.annotation = annotation;
	}

	@Override
	public boolean hasProperty(String prop) {
		return ASMUtils.hasAnnotationProperty(annotation, prop);
	}

	@Override
	public <T> T getProperty(String prop, T defaultValue) {
		return ASMUtils.getAnnotationProperty(annotation, prop, defaultValue);
	}
}
