package de.take_weiland.mods.commons.sync

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * @author diesieben07
 */

fun main(args: Array<String>) {
    val accessor = computeAccessors(Test::class.java)
    val x = accessor.invoke(0)
    println(x)
}

internal fun <T : SyncEnabled> computeAccessors(cls: Class<T>): MethodHandle {
    val accessors = cls.declaredFields.asSequence()
            .filter { !Modifier.isStatic(it.modifiers) }
            .filter { it.name.endsWith("\$delegate") }
            .filter { it.type.isSubtypeOf(SyncedProperty::class.java) }
            .map { createAccessor(it) }
            .toList().toTypedArray()

    return createSwitchingAccessor(accessors)
}

private val syncedPropertyArrayType = emptyArray<MethodHandle>().javaClass

private fun createSwitchingAccessor(handles: Array<MethodHandle>): MethodHandle {
    val arrayElementGetter = MethodHandles.arrayElementGetter(Array<MethodHandle>::class.java) // (Array<MethodHandle>, Int) -> MethodHandle
    val boundGetter = MethodHandles.insertArguments(arrayElementGetter, 0, handles) // (Int) -> MethodHandle
    val invoker = MethodHandles.exactInvoker(MethodType.methodType(SyncedProperty::class.java, Int::class.javaPrimitiveType)) // (MethodHandle, Int) -> SyncedProperty
    val combined = MethodHandles.filterArguments(invoker, 0, boundGetter) // (Int, Int) -> SyncedProperty
    val reordered = MethodHandles.permuteArguments(combined, MethodType.methodType(SyncedProperty::class.java, Int::class.javaPrimitiveType), 0, 0) // (Int) -> SyncedProperty
    return reordered
}

private fun createAccessor(field: Field): MethodHandle {
    field.isAccessible = true
    return MethodHandles.publicLookup().unreflectGetter(field)
            .asType(MethodType.methodType(SyncedProperty::class.java, Any::class.java))
}

private fun Class<*>.isSubtypeOf(other: Class<*>): Boolean {
    return other.isAssignableFrom(this)
}