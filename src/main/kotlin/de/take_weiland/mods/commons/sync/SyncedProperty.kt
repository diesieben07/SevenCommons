package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.util.immutableSetOf
import net.minecraft.tileentity.TileEntity
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class BaseSyncedProperty<T, SELF : BaseSyncedProperty<T, SELF>> internal constructor() {

    private lateinit var obj: Any
    private lateinit var property: KProperty1<*, T>
    private var id0: Int = -1
    val id: Int
        get() = id0.let {
//            if (it != -1) it else getId(property as KProperty1<*, *>, obj).also { id0 = it }
            0
        }

    fun <R : Any> internalInit(obj: R, clazz: Class<R>, property: KProperty<*>): Unit {
        this.obj = obj
        this.property = property as KProperty1<*, T>
    }

    inline operator fun <reified R : Any> provideDelegate(obj: R, property: KProperty<*>): SELF {
        internalInit(obj, R::class.java, property)
        return this as SELF
    }
}

abstract class SyncedProperty<T> : BaseSyncedProperty<T, SyncedProperty<T>>() {

    abstract operator fun getValue(obj: Any, property: KProperty<*>): T
    abstract operator fun setValue(obj: Any, property: KProperty<*>, newValue: T)

}

fun <T, C> BaseSyncedProperty<T, *>.markDirty(self: C) {

}

// ID is built as follows:
//      - Lowest 3 bits are class ID, starting with 0 at the top. Top is the class extending from e.g. TileEntity.
//        Classes without synced properties are still counted
//        This allows for a 7-deep hierarchy, which should be plenty
//      - Rest is property id, in order of definition in the source file.
// This allows classes to add synced properties without changing other classes' IDs
// The ID will fit in one VarInt byte so long as there are no more than 16 synced properties in a class
// Longer IDs appear on a per-class basis, so only classes with more than 16 properties get longer IDs.

private val idCache = ConcurrentHashMap<KProperty<*>, Int>()

private fun <T : Any> KProperty1<T, *>.getId(clazz: KClass<T>, obj: T): Int {
    return idCache.computeIfAbsent(this, {
        val propId = SyncedPropertyJavaHelper.getPropertyId(this, clazz, obj)
                .takeIf { it >= 0 }
                .let { it ?: throw IllegalArgumentException("Property not part of class?") }
        (propId shl CLASS_ID_BITS) or clazz.id
    })
}

private const val CLASS_ID_BITS = 3
private const val MAX_CLASS_ID = (1 shl (CLASS_ID_BITS + 1)) - 1
private val classHierarchyIgnored = immutableSetOf(Object::class.java, TileEntity::class.java)

private val KClass<*>.id: Int
    get() {
        return (this.java.hierarchy()
                .filter { it !in classHierarchyIgnored }
                .count() - 1)
                .takeIf { it in 0..MAX_CLASS_ID }
                .let { it ?: throw UnsupportedOperationException("Class hierarchy too deep for synced properties") }
    }

private fun <T> Class<T>.hierarchy() = generateSequence<Class<in T>>(this) { it.superclass }


fun test(bla: Float) {
    println(bla)
}

fun main(args: Array<String>) {
    println(Integer.toBinaryString(Test::blubb.getId(Test::class, Foo())))
}
