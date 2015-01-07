package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
abstract class MemberTypeSpecification<MEM extends AnnotatedElement & Member, T> implements TypeSpecification<T> {

	private final SerializationMethod serializationMethod;
	final MEM member;
	private TypeToken<T> type;

	MemberTypeSpecification(SerializationMethod serializationMethod, MEM member) {
		this.serializationMethod = serializationMethod;
		this.member = member;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypeToken<T> getType() {
		if (type == null) {
			type = (TypeToken<T>) TypeToken.of(getGenericType0());
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? super T> getRawType() {
		return (Class<? super T>) getRawType0();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return member.getAnnotation(annotationClass);
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return member.isAnnotationPresent(annotation);
	}

	@Override
	public SerializationMethod getDesiredMethod() {
		return serializationMethod;
	}

	abstract Class<?> getRawType0();
	abstract Type getGenericType0();

}