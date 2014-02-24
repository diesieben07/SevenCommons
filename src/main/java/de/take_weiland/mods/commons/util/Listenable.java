package de.take_weiland.mods.commons.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Listenable<SELF extends Listenable<SELF>> {

	public static interface Listener<E> {
		
		void onChange(E obj);
		
	}

	@Retention(RetentionPolicy.CLASS)
	@Target(ElementType.METHOD)
	public static @interface OnChange { }
	
}
