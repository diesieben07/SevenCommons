package de.take_weiland.mods.commons.net.simple

/**
 * Builder for sending a [Sendable] to multiple recipients. Usually used indirectly via [Sendable.sendMulti].
 *
 * @param MultiResult the result of this multi-send operation
 * @param Builder the type of this builder
 *
 * @author diesieben07
 */
interface MultiResultBuilder<out Result, out MultiResult, out Builder : MultiResultBuilder<Result, MultiResult, Builder>> : Sendable<Result, MultiResult, Builder> {

    /**
     * Finish this multi-send operation.
     */
    fun finish(): MultiResult

}