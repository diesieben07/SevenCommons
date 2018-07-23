package de.takeweiland.mods.commons.net2

/**
 * @author Take Weiland
 */
interface PayloadHandlerRegistry {

    fun getHandler(channel: String): CustomPayloadHandler?

}