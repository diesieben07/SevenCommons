package de.take_weiland.mods.commons.sync;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Mark a property to be automatically synchronized between client and server.</p>
 * <p>This works for TileEntities, Entities, Containers and IExtendedEntityProperties.</p>
 * <p>If this is used on a field that field must not be final. If it is used on a method, that method must
 * be a valid getter (no parameters, non-void return type) and there must be a valid, corresponding setter
 * obeying the rules specified in {@link de.take_weiland.mods.commons.asm.ASMUtils#findSetter(org.objectweb.asm.tree.ClassNode,
 * org.objectweb.asm.tree.MethodNode) ASMUtils.findSetter}.</p>
 * <p>Properties can be of any visibility.</p>
 * <p>The type of the property is determined via the declared type of the field resp. the return type of the getter.</p>
 * <p>The following types are supported by default:</p>
 * <ul>
 *     <li>All primitives and their wrapper types</li>
 *     <li>Strings</li>
 *     <li>ItemStacks and FluidStacks</li>
 *     <li>Enums</li>
 *     <li>EnumSets</li>
 *     <li>BitSets</li>
 *     <li>UUIDs</li>
 * </ul>
 * <p>Support for other types can be added by registering a {@link de.take_weiland.mods.commons.sync.ValueSyncer} for that type in the
 * {@link de.take_weiland.mods.commons.sync.Syncing} class.</p>
 * <p>If the value of this property changes, the old value will not be re-used even if possible. That means that if e.g.
 * an EnumSet changes, there will be a <i>new</i> EnumSet inserted on the client-side with the new contents instead of
 * the old EnumSet being updated with the new contents. If this behavior is not desired, use {@link de.take_weiland.mods.commons.sync.SyncContents}
 * instead.</p>
 *
 * <p>Note: This annotation works by using bytecode manipulation and will only work on classes that are not excluded from
 * class transformers.</p>
 *
 * @see de.take_weiland.mods.commons.sync.SyncContents
 * @author diesieben07
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Sync {

}
