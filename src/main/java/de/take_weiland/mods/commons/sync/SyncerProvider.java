package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.properties.ClassProperty;

/**
 * @author diesieben07
 */
public interface SyncerProvider {

    <S> Syncer<S> apply(ClassProperty<S> element);

    public static interface ForValue extends SyncerProvider {

        @Override
        <S> ValueSyncer<S> apply(ClassProperty<S> element);

    }

    public static interface ForContents extends SyncerProvider {

        @Override
        <S> ContentSyncer<S> apply(ClassProperty<S> element);

    }

}
