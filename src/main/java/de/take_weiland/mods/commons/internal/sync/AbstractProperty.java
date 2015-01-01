package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.SyncableProperty;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
abstract class AbstractProperty<MEM extends Member & AnnotatedElement> implements SyncableProperty<Object, Object> {

	final MEM member;

	AbstractProperty(MEM member) {
		this.member = member;
	}

	abstract TypeToken<?> resolveType();

	private TypeToken<?> typeToken;

	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<Object> getType() {
		TypeToken<?> tt = typeToken;
		if (tt == null) {
			synchronized (this) {
				if (typeToken == null) {
					typeToken = resolveType();
				}
			}
			tt = typeToken;
		}

		return (TypeToken<Object>) tt;
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
