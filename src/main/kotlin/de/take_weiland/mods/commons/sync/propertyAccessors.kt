package de.take_weiland.mods.commons.sync

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * @author diesieben07
 */
internal object PropertyAccessorCache : ClassValue<MethodHandle>() {

    override fun computeValue(type: Class<*>): MethodHandle {
        val accessors = type.declaredFields.asSequence()
                .filter { !Modifier.isStatic(it.modifiers) }
                .filter { it.name.endsWith("\$delegate") }
                .filter { it.type.isSubtypeOf(SyncedProperty::class.java) }
                .map { createAccessor(it) }
                .toList().toTypedArray()

        return if (accessors.isEmpty()) {
            val exceptionConstructor = MethodHandles.publicLookup().findConstructor(IllegalArgumentException::class.java, methodType(Void.TYPE, String::class.java))
            val boundExceptionConstructor = exceptionConstructor.bindTo("Class ${type.name} has no synced properties.")
            val thrower = MethodHandles.throwException(SyncedProperty::class.java, IllegalArgumentException::class.java)
            val combinedThrower = MethodHandles.filterReturnValue(boundExceptionConstructor, thrower)
            MethodHandles.dropArguments(combinedThrower, 0, Any::class.java, Int::class.javaPrimitiveType)
        } else {
            createSwitchingAccessor(accessors)
        }
    }
}


private fun createSwitchingAccessor(handles: Array<MethodHandle>): MethodHandle {
    val arrayElementGetter = MethodHandles.arrayElementGetter(Array<MethodHandle>::class.java) // (Array<MethodHandle>, Int) -> MethodHandle
    val boundGetter = MethodHandles.insertArguments(arrayElementGetter, 0, handles) // (Int) -> MethodHandle
    val invoker = MethodHandles.exactInvoker(MethodType.methodType(SyncedProperty::class.java, Object::class.java)) // (MethodHandle, Object) -> SyncedProperty
    val combined = MethodHandles.filterArguments(invoker, 0, boundGetter) // (Int, Object) -> SyncedProperty
    val reordered = MethodHandles.permuteArguments(combined, MethodType.methodType(SyncedProperty::class.java, Object::class.java, Int::class.javaPrimitiveType), 1, 0) // (Object, Int) -> SyncedProperty
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