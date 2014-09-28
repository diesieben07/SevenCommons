package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.AnnotationNull;

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
 * <p>The type of the property is determined via the declared type of the field. If that is not desired, use the
 * {@link #syncAs()} property to define the actual type, which must be runtime-assignable to the declared field type.</p>
 * <p>The following types are supported by default:</p>
 * <ul>
 *     <li>All primitives and their wrapper types</li>
 *     <li>Strings</li>
 *     <li>ItemStacks and FluidStacks</li>
 *     <li>Enums</li>
 *     <li>BitSets</li>
 *     <li>UUIDs</li>
 * </ul>
 * <p>If the property can never be null, you can set {@link #nullable()} to false, which may allow the system to do some
 * optimizations. That action is equivalent to applying the {@link org.jetbrains.annotations.NotNull} annotation
 * to the property.</p>
 *
 * @author diesieben07
 */
@Retention(RUNTIME) // need it via ClassInfo
@Target({ FIELD, METHOD })
public @interface Sync {

}
