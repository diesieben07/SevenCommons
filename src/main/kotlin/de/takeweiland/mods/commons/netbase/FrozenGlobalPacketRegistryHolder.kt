package de.takeweiland.mods.commons.netbase

import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.LoaderState

/**
 * @author Take Weiland
 */
@JvmField
internal val frozenGlobalPayloadHandlerRegistry: Map<String, CustomPayloadHandler> = run {
    check(Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
        "Tried to initialize frozenGlobalPayloadHandlerRegistry before init phase."
    }
    globalPayloadHandlerRegistry.freeze()
}
