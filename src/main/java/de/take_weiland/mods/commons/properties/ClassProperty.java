package de.take_weiland.mods.commons.properties;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;

/**
 * @author diesieben07
 */
public interface ClassProperty<T> extends AnnotatedElement {

	TypeToken<T> getType();

}
