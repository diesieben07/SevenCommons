package de.take_weiland.mods.commons.proxy

import de.take_weiland.mods.commons.util.physicalSide

/**
 * @author diesieben07
 */
typealias ProxyConstructor<T> = () -> T

inline fun <T> sidedProxy(crossinline clientSide: ProxyConstructor<T>, crossinline serverSide: ProxyConstructor<T>): T {
    return if (physicalSide.isClient) {
        object : () -> T {
            override fun invoke() = clientSide()
        }()
    } else {
        object : () -> T {
            override fun invoke() = serverSide()
        }()
    }
}