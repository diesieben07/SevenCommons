package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.PropertyMetadata;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
final class MetadataOverrideType implements PropertyMetadata {

	private final PropertyMetadata wrapped;
	private final Class<?> type;
	private TypeToken<?> typeToken;

	MetadataOverrideType(PropertyMetadata wrapped, Class<?> type) {
		this.wrapped = wrapped;
		this.type = type;
	}

	@Override
	public TypeToken<?> getType() {
		return typeToken == null ? (typeToken = wrapped.getType().getSubtype(type)) : typeToken;
	}

	@Override
	public Class<?> getRawType() {
		return type;
	}

	@Override
	public String toString() {
		return wrapped.toString() + " as type " + type.getName();
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return wrapped.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return wrapped.getAnnotation(annotationClass);
	}

	@NotNull
	@Override
	public Annotation[] getAnnotations() {
		return wrapped.getAnnotations();
	}

	@NotNull
	@Override
	public Annotation[] getDeclaredAnnotations() {
		return wrapped.getDeclaredAnnotations();
	}
}
