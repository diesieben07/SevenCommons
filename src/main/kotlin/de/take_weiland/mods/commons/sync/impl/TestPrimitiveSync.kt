package de.take_weiland.mods.commons.sync.impl

import de.take_weiland.mods.commons.sync.SyncedPropertyImmutable

/**
 * @author diesieben07
 */
fun <CONTAINER> CONTAINER.sync(value: Int): SyncedPropertyImmutable<CONTAINER, Int> {
    return SyncedPropertyImmutable(this, value)
}