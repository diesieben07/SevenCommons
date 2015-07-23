package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;

import java.util.function.Consumer;

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
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ForgeEventHandler.forceIEEPUpdate((EntityPlayerMP) event.player, event.player);
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
        //noinspection unchecked
        schedulerTick = (Consumer<Scheduler>) Launch.blackboard.remove(SCHEDULER_TEMP_KEY);
    }

    private static final Consumer<Scheduler> schedulerTick;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void serverTickClient(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            schedulerTick.accept(Scheduler.server());
            ASMHooks.tickIEEPCompanionsClientSide();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void serverTickServer(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            schedulerTick.accept(Scheduler.server());
            ASMHooks.tickIEEPCompanionsServerSide();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            schedulerTick.accept(Scheduler.client());
        }
    }

}
