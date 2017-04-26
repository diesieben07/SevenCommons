package de.take_weiland.mods.commons.sync;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
final class SyncCapHolder {

    // yay.
    @SuppressWarnings("ConstantConditions")
    @CapabilityInject(SyncCapability.class)
    @Nonnull
    static final Capability<SyncCapability<?>> SYNC_CAP_KEY = null;

}
