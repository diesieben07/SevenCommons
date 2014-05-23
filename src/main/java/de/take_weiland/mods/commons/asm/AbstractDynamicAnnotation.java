package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AnnotationNode;

/**
 * @author diesieben07
 */
abstract class AbstractDynamicAnnotation {

	static final String FIELD_NAME = "node";

	final AnnotationNode node;

	AbstractDynamicAnnotation(AnnotationNode node) {
		this.node = node;
	}
}
