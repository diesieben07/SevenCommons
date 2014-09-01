package de.take_weiland.mods.commons.sync;

/**
 * <p>Marker interface for {@link de.take_weiland.mods.commons.sync.Syncable} classes. This should be referred to instead
 * of {@link de.take_weiland.mods.commons.sync.Syncable} to also support users of
 * {@link de.take_weiland.mods.commons.sync.MakeSyncable}. Convert to a {@code Syncable} using
 * {@link de.take_weiland.mods.commons.sync.Syncing#asSyncable(SyncableProxy)}.</p>
 * <p><strong>Do not implement this interface directly!</strong></p>
 *
 * @see de.take_weiland.mods.commons.sync.Syncable
 * @see de.take_weiland.mods.commons.sync.MakeSyncable
 *
 * @author diesieben07
 */
public interface SyncableProxy { }
