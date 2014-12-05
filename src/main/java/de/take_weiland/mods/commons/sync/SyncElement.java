package de.take_weiland.mods.commons.sync;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;

/**
 * @author diesieben07
 */
public interface SyncElement<T> extends AnnotatedElement {

	TypeToken<T> getType();

	<S> Optional<SyncElement<S>> ofType(Class<S> type);

	<S> Optional<SyncElement<S>> ofType(TypeToken<S> type);

}
