package de.take_weiland.mods.commons.fastreflect;

import com.google.common.base.Preconditions;

import de.take_weiland.mods.commons.util.JavaUtils;

public final class Fastreflect {

	public static <T> T createAccessor(Class<T> iface) {
		return factory.createAccessor(Preconditions.checkNotNull(iface));
	}
	
	private static final AccessorFactory factory;
	
	static {
		factory = selectFactory();
	}

	private static AccessorFactory selectFactory() {
		if (JavaUtils.hasUnsafe()) {
			try {
				return Class.forName("de.take_weiland.mods.commons.fastreflect.MagicAccessorFactory").asSubclass(AccessorFactory.class).newInstance();
			} catch (Exception e) {
				// then not
			}
		}
		System.out.println("Using slow AccessorFactory!");
		return new ReflectiveAccessorFactory();
	}
	
}
