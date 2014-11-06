package de.take_weiland.mods.commons.asm.info;

import de.take_weiland.mods.commons.asm.ASMUtils;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
class MethodInfoASM extends MethodInfo {

	private final MethodNode method;

	MethodInfoASM(ClassInfo clazz, MethodNode method) {
		super(clazz);
		this.method = method;
	}

	@Override
	public String name() {
		return method.name;
	}

	@Override
	public String desc() {
		return method.desc;
	}

	@Override
	public int modifiers() {
		return method.access;
	}

	@Override
	public AnnotationInfo getAnnotation(Class<? extends Annotation> annotation) {
		AnnotationNode ann = ASMUtils.getAnnotation(method, annotation);
		return ann == null ? null : new AnnotationInfoASM(this, ann);
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return ASMUtils.hasAnnotation(method, annotation);
	}
}
