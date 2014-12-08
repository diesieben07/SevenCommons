package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.properties.ClassProperty;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * @author diesieben07
 */
public abstract class ClassPropertyMember<MEM extends AnnotatedElement, T> implements ClassProperty<T> {

	protected final MEM member;
	private TypeToken<T> type;

	protected ClassPropertyMember(MEM member) {
		this.member = member;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<T> getType() {
		return type == null ? (type = (TypeToken<T>) resolveType()) : type;
	}

	protected abstract TypeToken<?> resolveType();

	@Nonnull
	@Override
	public Annotation[] getDeclaredAnnotations() {
		return member.getDeclaredAnnotations();
	}

	@Nonnull
	@Override
	public Annotation[] getAnnotations() {
		return member.getAnnotations();
	}

	@Override
	public <A extends Annotation> A getAnnotation(@Nonnull Class<A> annotationClass) {
		return member.getAnnotation(annotationClass);
	}

	@Override
	public boolean isAnnotationPresent(@Nonnull Class<? extends Annotation> annotationClass) {
		return member.isAnnotationPresent(annotationClass);
	}
}
