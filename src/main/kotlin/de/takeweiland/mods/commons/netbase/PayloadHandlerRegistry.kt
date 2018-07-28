package de.takeweiland.mods.commons.netbase

/**
 * @author Take Weiland
 */
interface PayloadHandlerRegistry {

    fun getHandler(channel: String): CustomPayloadHandler?

    interface Mutable : PayloadHandlerRegistry {

        fun register(channel: String, handler: CustomPayloadHandler)

    }

}