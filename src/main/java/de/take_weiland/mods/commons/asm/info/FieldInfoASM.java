package de.take_weiland.mods.commons.asm.info;

import de.take_weiland.mods.commons.asm.ASMUtils;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
class FieldInfoASM extends FieldInfo {

	private final FieldNode field;

	FieldInfoASM(ClassInfo clazz, FieldNode field) {
		super(clazz);
		this.field = field;
	}

	@Override
	public String desc() {
		return field.desc;
	}

	@Override
	public String name() {
		return field.name;
	}

	@Override
	public int modifiers() {
		return field.access;
	}

	@Override
	public AnnotationInfo getAnnotation(Class<? extends Annotation> annotation) {
		AnnotationNode ann = ASMUtils.getAnnotation(field, annotation);
		return ann == null ? null : new AnnotationInfoASM(this, ann);
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return ASMUtils.hasAnnotation(field, annotation);
	}
}
