package de.take_weiland.mods.commons.sync;

/**
 * <p>Similar to {@link de.take_weiland.mods.commons.sync.Sync}, but instead this annotation will synchronize the
 * <i>contents</i> of a property without ever updating the value of the property itself. As such this Annotation can be
 * used on final fields or getters without a corresponding setter, but the value of the property must never be null.</p>
 * <p>As an Example: If an EnumSet were to be marked with {@code &#064;SyncContents} an update would be roughly equivalent
 * to the following code:<pre><code>
 *     &#064;SyncContents EnumSet property = ...;
 *
 *     void update(EnumSet newContents) {
 *         property.clear();
 *         property.addAll(newContents);
 *     }
 * </code></pre></p>
 * <p>The following types are supported by default:</p>
 * <ul>
 *     <li>ItemStacks and FluidStacks</li>
 *     <li>FluidTanks (<i>not</i> IFluidTank, see also: {@link de.take_weiland.mods.commons.sync.SyncCapacity})</li>
 *     <li>BitSets</li>
 *     <li>EnumSets</li>
 * </ul>
 * <p>Support for other types can be added by registering a {@link de.take_weiland.mods.commons.sync.ContentSyncer} for that type in the
 * {@link de.take_weiland.mods.commons.sync.Syncing} class.</p>
 * @author diesieben07
 */
public @interface SyncContents {
}
