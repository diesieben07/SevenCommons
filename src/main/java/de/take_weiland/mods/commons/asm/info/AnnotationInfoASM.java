package de.take_weiland.mods.commons.asm.info;

import com.google.common.base.Optional;
import de.take_weiland.mods.commons.asm.ASMUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

/**
 * @author diesieben07
 */
class AnnotationInfoASM extends AnnotationInfo {

	private final AnnotationNode annotation;

	AnnotationInfoASM(HasAnnotations holder, AnnotationNode annotation) {
		super(holder);
		this.annotation = annotation;
	}

	@Override
	public Type type() {
		return Type.getType(annotation.desc);
	}

	@Override
	public boolean hasProperty(String prop) {
		return ASMUtils.hasAnnotationProperty(annotation, prop);
	}

	@Override
	public <T> Optional<T> getProperty(String prop) {
		return ASMUtils.getAnnotationProperty(annotation, prop);
	}
}
