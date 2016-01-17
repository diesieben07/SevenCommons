package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.client.worldview.WorldViewImpl;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.worldview.ChunkUpdateTracker;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * @author diesieben07
 */
public final class FMLEventHandler {

    public static final String INV_IN_USE_KEY = "_sc$iteminv$inUse";

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (getMinecraft().theWorld != null && event.phase == TickEvent.Phase.START) {
            WorldViewImpl.renderAll();
        }
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(SevenCommons.MOD_ID)) {
            SevenCommons.syncConfig(false);
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isServer() && event.player.openContainer == event.player.inventoryContainer) {
            ItemStack current = event.player.inventory.getCurrentItem();
            if (current != null && current.stackTagCompound != null) {
                current.stackTagCompound.removeTag(INV_IN_USE_KEY);
            }
        }
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ChunkUpdateTracker.postWorldTick(event.world);
        }
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ForgeEventHandler.forceIEEPUpdate((EntityPlayerMP) event.player, event.player);
        UsernameCache.onPlayerLogin(event.player);
    }

    @SubscribeEvent
    public void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerChunkViewManager.onPlayerLogout(event.player);
    }

    @SubscribeEvent
    public void serverConnectionFromClient(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        NetworkImpl.handleServersideConnection(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        NetworkImpl.handleClientsideConnection(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void serverTickClient(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ((SchedulerBase) Scheduler.server()).tick();
            ASMHooks.tickIEEPCompanionsClientSide();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void serverTickServer(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ((SchedulerBase) Scheduler.server()).tick();
            ASMHooks.tickIEEPCompanionsServerSide();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ((SchedulerBase) Scheduler.client()).tick();
        }
    }

}
