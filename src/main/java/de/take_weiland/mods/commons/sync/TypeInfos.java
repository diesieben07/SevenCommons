package de.take_weiland.mods.commons.sync;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
public final class TypeInfos {

	public static SyncTypeInfo getIterableElements(SyncTypeInfo iterable) {
		return new TypeInfoGenericParameter(iterable, Iterable.class.getTypeParameters()[0]);
	}

	public static SyncTypeInfo getArrayComponent(SyncTypeInfo array) {
		return new TypeInfoArrayElements(array);
	}

	public static SyncTypeInfo forField(Field field) {
		return new TypeInfoField(field);
	}

	public static SyncTypeInfo forMethod(Method method) {
		return new TypeInfoMethod(method);
	}

	public static SyncTypeInfo forType(Type type) {
		return new TypeInfoDirect(type);
	}

	private TypeInfos() { }

}
