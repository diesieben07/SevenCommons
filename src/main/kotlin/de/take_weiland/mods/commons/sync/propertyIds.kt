/**
 * @author diesieben07
 */
package de.take_weiland.mods.commons.sync

import com.google.common.collect.ImmutableSet
import net.minecraft.entity.Entity
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.internal.PropertyReference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

// ID is built as follows:
//      - Lowest 3 bits are class ID, starting with 0 at the top. Top is the class extending from e.g. TileEntity.
//        Classes without synced properties are still counted
//        This allows for a 7-deep hierarchy, which should be plenty
//      - Rest is property id, in order of definition in the source file.
// This allows classes to add synced properties without changing other classes' IDs
// The ID will fit in one VarInt byte so long as there are no more than 16 synced properties in a class
// Longer IDs appear on a per-class basis, so only classes with more than 16 properties get longer IDs.

fun KProperty1<*, *>.getPropertyId(): Int {
    return propertyIdCache[this] ?: propertyIdCache.computeIfAbsent(this) {
        val owner = (this as PropertyReference).owner
        require(owner is KClass<*>) { "Can only use synced properties inside a class." }
        val declaringClass = (owner as KClass<*>).java // this works because we are only called from a delegated property
        val delegatedPropertiesField = declaringClass.getDeclaredField("\$\$delegatedProperties")
        delegatedPropertiesField.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val delegatedProperties = delegatedPropertiesField[null] as Array<KProperty<*>>

        val idInClass = delegatedProperties.indexOfFirst { it == this }
        if (idInClass < 0) throw IllegalStateException("Property not in it's declaring class?")
        declaringClass.getClassId() or (idInClass shl CLASS_ID_BITS)
    }
}

private val propertyIdCache = ConcurrentHashMap<KProperty1<*, *>, Int>()

private const val CLASS_ID_BITS = 3
private const val MAX_CLASS_ID = (1 shl (CLASS_ID_BITS + 1)) - 1
private val IGNORED_CLASSES = ImmutableSet.of(TileEntity::class.java, Entity::class.java, Container::class.java)

private fun <T : Any> Class<T>.getClassId(): Int {
    var id = 0
    var currentClass : Class<*> = this
    do {
        currentClass = currentClass.superclass
        if (currentClass in IGNORED_CLASSES) continue
        if (currentClass == Object::class.java) break
        id++
    } while (true)

    if (id > MAX_CLASS_ID) throw UnsupportedOperationException("Class hierarchy too deep for synced properties") else return id
}