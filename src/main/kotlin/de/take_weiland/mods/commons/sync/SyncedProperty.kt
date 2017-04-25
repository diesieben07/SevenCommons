package de.take_weiland.mods.commons.sync

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class BaseSyncedProperty<T> {

    private lateinit var obj: Any
    private lateinit var property: KProperty<*>
    private var id0: Int = -1
    val id: Int
        get() = id0.let {
//            if (it != -1) it else getId(property as KProperty1<*, *>, obj).also { id0 = it }
            0
        }

    open fun provideDelegate(obj: Any, property: KProperty<*>): BaseSyncedProperty<T> = this

//    inline operator fun <reified T : Any>provideDelegate(obj: T, property: KProperty<*>): BaseSyncedProperty<T> {
//        this.obj = obj
//        this.property = property
//        return this
//    }
}

abstract class SyncedProperty<T> : BaseSyncedProperty<T>() {

    abstract operator fun getValue(obj: Any, property: KProperty<*>): T
    abstract operator fun setValue(obj: Any, property: KProperty<*>, newValue: T)

    override fun provideDelegate(obj: Any, property: KProperty<*>): SyncedProperty<T> {
        super.provideDelegate(obj, property)
        return this
    }
}

fun <T, C> BaseSyncedProperty<T>.markDirty(self: C) {

}

private val idCache = ConcurrentHashMap<KProperty<*>, Int>()

private val classIdCache = ConcurrentHashMap<Class<*>, Int>()
private fun <T : Any> KProperty1<in T, *>.getId(obj: T): Int {
    return idCache.computeIfAbsent(this, {
        0
    })
}

private const val MAX_CLASS_ID = 7
private val classHierarchyIgnored = setOf(Object::class.java)

private val KClass<*>.id: Int
    get() {
        return (generateSequence<Class<*>>(this.java) { it.superclass }
                .filter { it != Object::class.java }
                .also { it.forEach(::println) }
                .count() - 1).let { if (it in 0..MAX_CLASS_ID) it else throw UnsupportedOperationException("Class hierarchy too deep for synced properties") }
    }



private fun <T : Any> KClass<T>?.getNextFreeId(obj: Any): Int {
//    return (this?.getSyncedProperties(obj as T)?.count() ?: 0) + ((this?.superclass as? KClass<Any>)?.getNextFreeId(obj) ?: 0)
    return 0;
}

private val <T : Any> KClass<in T>.superclass: KClass<in T>?
    get() = (java.superclass as? Class<out Any>)?.kotlin as? KClass<in T>

private fun <T : Any> KClass<T>.getSyncedProperties(obj: T) = SyncedPropertyJavaHelper.getSyncedProperties(this, obj)

fun test(bla: Float) {
    println(bla)
}

fun main(args: Array<String>) {
    println(Test::class.id)
}
