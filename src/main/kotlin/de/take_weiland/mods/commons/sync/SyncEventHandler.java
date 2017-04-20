package de.take_weiland.mods.commons.sync;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author diesieben07
 */
@Mod.EventBusSubscriber
public class SyncEventHandler {

    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent event) {
//        if (NeedsSyncCap.INSTANCE.get(event.getObject().getClass())) {
//            event.addCapability(new ResourceLocation(SevenCommons.MOD_ID, "sync"), new SyncCapability());
//        }
    }

}
