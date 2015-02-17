package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncableProperty;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Map;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
abstract class AbstractProperty<MEM extends Member & AnnotatedElement> implements SyncableProperty<Object, Object> {

	final MEM member;
	private final SerializationMethod.Method method;

	AbstractProperty(MEM member, Class<? extends Annotation> annotationClass) {
		this.member = member;
		this.method = getMethodFromAnnotation(getAnnotation(annotationClass));
	}

	abstract TypeToken<?> resolveType();

	private TypeToken<?> typeToken;

	@Override
	public final SerializationMethod.Method getDesiredMethod() {
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

	@SuppressWarnings("unchecked")
	private static <A extends Annotation> SerializationMethod.Method getMethodFromAnnotation(A ann) {
		return ((Function<A, SerializationMethod.Method>) methodGetters.get(ann.annotationType())).apply(ann);
	}

	private static final Map<Class<? extends Annotation>, Function<? extends Annotation, SerializationMethod.Method>> methodGetters;

	static {
		methodGetters = ImmutableMap.of(
				Sync.class, new Function<Sync, SerializationMethod.Method>() {
					@Override
					public SerializationMethod.Method apply(@Nullable Sync input) {
						assert input != null;
						return input.method();
					}
				},
				ToNbt.class, new Function<ToNbt, SerializationMethod.Method>() {
					@Override
					public SerializationMethod.Method apply(@Nullable ToNbt input) {
						assert input != null;
						return input.method();
					}

				}
		);
	}

}
