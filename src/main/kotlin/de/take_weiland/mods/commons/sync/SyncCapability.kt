package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.sync.SyncCapHolder.SYNC_CAP_KEY
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider

class SyncCapability : ICapabilityProvider {

    internal var changed: Int = 0

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? = (this as T).takeIf { capability === SYNC_CAP_KEY }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean = capability === SYNC_CAP_KEY
}