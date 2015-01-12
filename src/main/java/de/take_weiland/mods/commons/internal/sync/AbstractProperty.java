package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.sync.SyncableProperty;

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
	private final SerializationMethod method;

	AbstractProperty(MEM member, SerializationMethod method) {
		this.member = member;
		this.method = method;
	}

	abstract TypeToken<?> resolveType();

	private TypeToken<?> typeToken;

	@Override
	public final SerializationMethod getDesiredMethod() {
		return method;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final TypeToken<Object> getType() {
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
	public final boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
		return member.isAnnotationPresent(annotationClass);
	}

	@Override
	public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return member.getAnnotation(annotationClass);
	}

}
