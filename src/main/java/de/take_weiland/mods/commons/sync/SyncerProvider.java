package de.take_weiland.mods.commons.sync;

/**
 * @author diesieben07
 */
public interface SyncerProvider {

    <S> Syncer<S> apply(SyncElement<S> element);

    public static interface ForValue extends SyncerProvider {

        @Override
        <S> ValueSyncer<S> apply(SyncElement<S> element);

    }

    public static interface ForContents extends SyncerProvider {

        @Override
        <S> ContentSyncer<S> apply(SyncElement<S> element);

    }

}
