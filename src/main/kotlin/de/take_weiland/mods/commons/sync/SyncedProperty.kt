package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.util.isServer
import io.netty.buffer.ByteBuf
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

/**
 * Base class for property delegates for syncing. Support for syncing types can be added by subclassing this class.
 *
 * A change in property value is expressed as a value of type `PAYLOAD`.
 */
@Suppress("AddVarianceModifier")
abstract class SyncedProperty<PAYLOAD> {

    /**
     * The ID of this property. The ID is used to identify the property in network packets.
     */
    internal var id: Int = -1
        get() = if (field >= 0) field else throw IllegalStateException("SyncedProperty not initialized.")
        private set(value) {
            field = value
        }


    /**
     * Mark this property as "dirty", i.e. needing to be sent to the client. This method does nothing on the client.
     *
     * The payload object must not hold any references to side-specific data such as worlds, entities, etc. The object
     * may be passed directly through to the client-side property instance.
     *
     * @param obj the object on which the value changed as given by the `setValue` operator.
     * @param payload the payload value
     */
    fun markDirty(obj: Any, payload: PAYLOAD) {
        if (findContainerType(obj).getWorld(obj).isServer) {
            changesFor(obj).addChange(this, payload)
        }
    }

    /**
     * Serialize the given payload value to the buffer.
     *
     * @param buf the buffer
     * @param payload the payload value
     */
    abstract fun writePayload(buf: ByteBuf, payload: PAYLOAD)

    /**
     * Deserialize a payload value from the buffer and apply the change specified by the deserialized payload to this property.
     *
     * @param buf the buffer
     */
    abstract fun receivePayload(buf: ByteBuf)

    /**
     * Apply the change specified by the given payload value to this property.
     */
    abstract fun receivePayload(payload: PAYLOAD)

    @PublishedApi
    internal fun init(obj: Any, property: KProperty<*>) {
        if (property !is KMutableProperty1<*, *>) throw UnsupportedOperationException("Only mutable member properties in a class can be synced.")
        id = property.getPropertyId()
    }

}

private const val IS_SERVER_BIT: Int = 1 shl 31
private const val ID_MASK: Int = IS_SERVER_BIT.inv()

inline operator fun <T : SyncedProperty<*>> T.provideDelegate(obj: Any, property: KProperty<*>): T {
    init(obj, property)
    return this
}

interface TickingProperty {

    fun update()

}

// this does not need to be thread-safe, because it is only accessed from the server thread
internal val changedProperties = HashMap<Any, ChangedPropertyList<*>>()

private fun changesFor(obj: Any): ChangedPropertyList<*> = with(changedProperties) {
    val changes = this[obj]
    if (changes != null) return changes

    val newChangeList = findContainerType(obj).createChangedPropertyList(obj)
    this[obj] = newChangeList
    return newChangeList
}
