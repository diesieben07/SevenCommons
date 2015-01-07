package de.take_weiland.mods.commons.serialize;

import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
public interface TypeSpecification<T> {

	TypeToken<T> getType();

	Class<? super T> getRawType();

	SerializationMethod getDesiredMethod();

	boolean hasAnnotation(Class<? extends Annotation> annotation);

	<A extends Annotation> A getAnnotation(Class<A> annotationClass);

	final class Simple<T> implements TypeSpecification<T> {

		private final Class<T> type;
		private final SerializationMethod serializationMethod;
		private TypeToken<T> typeToken;

		public Simple(Class<T> type, SerializationMethod serializationMethod) {
			this.type = type;
			this.serializationMethod = serializationMethod;
		}

		@Override
		public TypeToken<T> getType() {
			if (typeToken == null) {
				typeToken = TypeToken.of(type);
			}
			return typeToken;
		}

		@Override
		public Class<T> getRawType() {
			return type;
		}

		@Override
		public SerializationMethod getDesiredMethod() {
			return serializationMethod;
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotation) {
			return false;
		}

		@Override
		public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
			return null;
		}
	}

}
