package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.PropertyMetadata;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
abstract class MemberMetadata<MEM extends AnnotatedElement & Member> implements PropertyMetadata {

	final MEM member;
	private TypeToken<?> type;

	MemberMetadata(MEM member) {
		this.member = member;
	}

	abstract Type getGenericType();

	@Override
	public TypeToken<?> getType() {
		return type == null ? (type = TypeToken.of(getGenericType())) : type;
	}

	@Override
	public String toString() {
		return member.getClass().getSimpleName() + " "
				+ member.getDeclaringClass().getName()
				+ "." + member.getName()
				+ " of type" + getType();
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return member.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return member.getAnnotation(annotationClass);
	}

	@Nonnull
	@Override
	public Annotation[] getAnnotations() {
		return member.getAnnotations();
	}

	@Nonnull
	@Override
	public Annotation[] getDeclaredAnnotations() {
		return member.getDeclaredAnnotations();
	}
}
