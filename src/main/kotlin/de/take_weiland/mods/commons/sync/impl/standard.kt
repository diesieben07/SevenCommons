//package de.take_weiland.mods.commons.sync.impl
//
//import de.take_weiland.mods.commons.sync.SyncedPropertyIdentityImmutable
//import de.take_weiland.mods.commons.sync.SyncedPropertyImmutable
//import de.take_weiland.mods.commons.sync.SyncedPropertyMutable
//import java.util.*
//
///**
// * @author diesieben07
// */
//fun <R : Any> R.sync(initialValue: String) = SyncedPropertyImmutable(initialValue, this)
//
//fun <R : Any> R.sync(initialValue: UUID) = SyncedPropertyImmutable(initialValue, this)
//
//fun <R : Any> R.sync(initialValue: BitSet) = object : SyncedPropertyMutable<BitSet, R>(initialValue, this) {
//    override fun BitSet.copy() = clone() as BitSet
//}
//
//fun <R : Any, E : Enum<E>> R.sync(initialValue: EnumSet<E>) = object : SyncedPropertyMutable<EnumSet<E>, R>(initialValue, this) {
//    override fun EnumSet<E>.copy() = clone()
//}
//
//fun <R : Any, E : Enum<E>> R.sync(initialValue: E) = SyncedPropertyIdentityImmutable(this, initialValue)