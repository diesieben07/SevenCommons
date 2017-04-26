package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author diesieben07
 */
// needs to be Java for raw types...
// needs raw types because generic events don't support <?>
@Mod.EventBusSubscriber
public class SyncEventHandler {

    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent event) {
        if (event.getObject() instanceof ICapabilityProvider) {
            event.addCapability(new ResourceLocation(SevenCommons.MOD_ID, "sync"), new SyncCapability<>((ICapabilityProvider) event.getObject()));
        }
    }

}
