package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.internal.client.worldview.WorldViewImpl;
import de.take_weiland.mods.commons.internal.worldview.ChunkUpdateTracker;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * @author diesieben07
 */
@Mod.EventBusSubscriber(modid = SevenCommons.MOD_ID)
public final class FMLEventHandler {

    public static final String INV_IN_USE_KEY = "_sc$iteminv$inUse";

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (getMinecraft().world != null && event.phase == TickEvent.Phase.END) {
            WorldViewImpl.renderAll();
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(SevenCommons.MOD_ID)) {
            SevenCommons.syncConfig(false);
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isServer() && event.player.openContainer == event.player.inventoryContainer) {
            ItemStack current = event.player.inventory.getCurrentItem();
            if (current != null && current.getTagCompound() != null) {
                current.getTagCompound().removeTag(INV_IN_USE_KEY);
            }
        }
    }

    @SubscribeEvent
    public static void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ChunkUpdateTracker.postWorldTick(event.world);
        }
    }

    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        UsernameCache.onPlayerLogin(event.player);
    }

    @SubscribeEvent
    public static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerChunkViewManager.onPlayerLogout(event.player);
    }
//
//    @SubscribeEvent
//    public static void serverConnectionFromClient(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
//        NetworkImpl.handleServersideConnection(event);
//    }
//
//    @SubscribeEvent
//    @SideOnly(Side.CLIENT)
//    public static void clientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
//        NetworkImpl.handleClientConnectedToServer(event);
//    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void clientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Scheduler.Companion.getClient().execute(WorldViewImpl::cleanup);
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ((SchedulerBase) Scheduler.Companion.getServer()).tick();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ((SchedulerBase) Scheduler.Companion.getClient()).tick();
            WorldViewImpl.tick();
        }
    }

}
