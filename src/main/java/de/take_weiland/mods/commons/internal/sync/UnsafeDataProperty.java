package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.util.JavaUtils;
import sun.misc.Unsafe;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * @author diesieben07
 */
abstract class UnsafeDataProperty<MEM extends Member & AnnotatedElement> extends AbstractProperty<MEM> {

	static final Unsafe unsafe = JavaUtils.getUnsafe();
	private final long dataOff;

	UnsafeDataProperty(MEM member, Field dataField, Class<? extends Annotation> annotationClass) {
		super(member, annotationClass);
		dataOff = unsafe.objectFieldOffset(dataField);
	}

	@Override
	public final void setData(Object data, Object instance) {
		unsafe.putObject(instance, dataOff, data);
	}

	@Override
	public final Object getData(Object instance) {
		return unsafe.getObject(instance, dataOff);
	}

}
