///**
// * @author diesieben07
// */
//package de.take_weiland.mods.commons.sync
//
//import com.google.common.collect.ImmutableSet
//import net.minecraft.entity.Entity
//import net.minecraft.inventory.Container
//import net.minecraft.tileentity.TileEntity
//import java.lang.reflect.Field
//import java.lang.reflect.Member
//import java.util.concurrent.ConcurrentHashMap
//import kotlin.reflect.KProperty
//import kotlin.reflect.KProperty1
//import kotlin.reflect.full.declaredMemberProperties
//import kotlin.reflect.jvm.javaField
//import kotlin.reflect.jvm.javaGetter
//
//// ID is built as follows:
////      - Lowest 3 bits are class ID, starting with 0 at the top. Top is the class extending from e.g. TileEntity.
////        Classes without synced properties are still counted
////        This allows for a 7-deep hierarchy, which should be plenty
////      - Rest is property id, in order of definition in the source file.
//// This allows classes to add synced properties without changing other classes' IDs
//// The ID will fit in one VarInt byte so long as there are no more than 16 synced properties in a class
//// Longer IDs appear on a per-class basis, so only classes with more than 16 properties get longer IDs.
//
//fun KProperty1<*, *>.getPropertyId(): Int {
//    return propertyIdCache[this] ?: propertyIdCache.computeIfAbsent(this) {
//        val declaringClass = declaringClass
//        val idInClass = declaringClass.kotlin.declaredMemberProperties.asSequence()
//                .filter { it.delegateType.let { it != null && SyncedProperty::class.java.isAssignableFrom(it) } }
//                .indexOf(this)
//
//        if (idInClass < 0) throw IllegalStateException("Property not in it's declaring class?")
//
//        declaringClass.getClassId() or (idInClass shl CLASS_ID_BITS)
//    }
//}
//
//private val propertyIdCache = ConcurrentHashMap<KProperty1<*, *>, Int>()
//
//private const val CLASS_ID_BITS = 3
//private const val MAX_CLASS_ID = (1 shl (CLASS_ID_BITS + 1)) - 1
//private val IGNORED_CLASSES = ImmutableSet.of(TileEntity::class.java, Entity::class.java, Container::class.java)
//
//private fun <T : Any> Class<T>.getClassId(): Int {
//    var id = 0
//    var currentClass : Class<*> = this
//    do {
//        currentClass = currentClass.superclass
//        if (currentClass in IGNORED_CLASSES) continue
//        if (currentClass == Object::class.java) break
//        id++
//    } while (true)
//
//    if (id > MAX_CLASS_ID) throw UnsupportedOperationException("Class hierarchy too deep for synced properties") else return id
//}
//
//private val KProperty<*>.declaringClass: Class<*>
//    get() = (javaField as Member? ?: javaGetter)?.declaringClass ?: throw IllegalArgumentException("Property does not have getter or field.")
//
//private val KProperty<*>.delegateField : Field?
//    get() = try {
//        declaringClass.getDeclaredField("$name\$delegate")
//    } catch (x: NoSuchFieldException) {
//        null
//    }
//
//private val KProperty<*>.delegateType : Class<*>?
//    get() = delegateField?.type
//
//fun main(args: Array<String>) {
//    println(Test::bla.delegateType)
//}