package de.take_weiland.mods.commons.sync

import kotlin.Int as kInt

/**
 * @author diesieben07
 */
sealed class ChangedProperty<T>(val property: SyncedProperty<T>) {

    abstract val valueGeneric: T

    class Obj<T>(property: SyncedProperty<T>, val value: T) : ChangedProperty<T>(property) {
        override val valueGeneric get() = value
    }

    class Int(property: SyncedProperty<kInt>, val value: kInt) : ChangedProperty<kInt>(property) {
        override val valueGeneric get() = value
    }

}

