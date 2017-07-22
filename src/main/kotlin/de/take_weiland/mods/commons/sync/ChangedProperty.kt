package de.take_weiland.mods.commons.sync

import io.netty.buffer.ByteBuf

/**
 * @author diesieben07
 */
abstract class ChangedProperty<PAYLOAD>(val property: SyncedProperty<PAYLOAD>) {

    abstract val payload: PAYLOAD

    fun write(buf: ByteBuf) {
        writeContainerData(buf)
        property.writePayload(buf, payload)
    }

    protected abstract fun writeContainerData(buf: ByteBuf)

}