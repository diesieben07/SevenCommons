package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * <p>A Watcher handles detecting changes in a {@link de.take_weiland.mods.commons.sync.SyncableProperty} and
 * encoding it's value into an {@linkplain de.take_weiland.mods.commons.net.MCDataOutput OutputStream}.</p>
 *
 * @author diesieben07
 */
public interface Watcher<T> {

    /**
     * <p>Initial setup for the given property. Called once per property and before any other methods.
     * This method is usually used to set-up any necessary data-objects for the property.</p>
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void setup(SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Write the value to the stream no matter if it has been changed or not.
     * This is used to send the "initial" value of a property to the client when the client starts tracking the property
     * (e.g. by entering a chunk).</p>
     * @param out the stream
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Determine if the given property has changed since the last call to {@code writeAndUpdate}.</p>
     * <p>This method is only called on the server.</p>
     * @param property the property
     * @param instance the instance
     * @return true if the property has changed
     */
    <OBJ> boolean hasChanged(SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Write the value of the property to the stream and mark it's current state as "up to date".</p>
     * <p>This method is only called on the server.</p>
     * @param out the stream
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Update the value of the property by reading the value from the stream.</p>
     * <p>This method is only called on the client.</p>
     * @param in the stream
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void read(MCDataInput in, SyncableProperty<T, OBJ> property, OBJ instance);

}
