package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.net.*;
import de.take_weiland.mods.commons.util.Scheduler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;

/**
 * @author diesieben07
 */
public final class FMLEventHandler {

    public static final String SCHEDULER_TEMP_KEY = "__sc_temp_scheduler_runnables";
    public static final String INV_IN_USE_KEY = "_sc$iteminv$inUse";

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
    public void serverConnectionFromClient(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        NetworkImpl.handleServersideConnection(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        NetworkImpl.handleClientsideConnection(event);
    }

    static {
        Reflection.initialize(Scheduler.class);
        Runnable[] tickers = (Runnable[]) Launch.blackboard.remove(SCHEDULER_TEMP_KEY);
        serverTick = tickers[0];
        clientTick = tickers[1];
    }

    private static final Runnable serverTick;
    private static final Runnable clientTick;

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            serverTick.run();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            clientTick.run();
        }
    }

}
