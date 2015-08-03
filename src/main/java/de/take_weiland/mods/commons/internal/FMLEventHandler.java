package de.take_weiland.mods.commons.internal;

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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * @author diesieben07
 */
public final class FMLEventHandler {

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
        try {
            // this is ugly but I don't want Scheduler.tick to be public for obvious reasons
            // and Scheduler can't extend a class from the internal package since it already extends
            // something else. This will perform just fine, so meh.
            Method method = Scheduler.class.getDeclaredMethod("tick");
            method.setAccessible(true);
            schedulerTick = publicLookup().unreflect(method);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle schedulerTick;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void serverTickClient(TickEvent.ServerTickEvent event) throws Throwable {
        if (event.phase == TickEvent.Phase.END) {
            schedulerTick.invokeExact(Scheduler.server());
            ASMHooks.tickIEEPCompanionsClientSide();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void serverTickServer(TickEvent.ServerTickEvent event) throws Throwable {
        if (event.phase == TickEvent.Phase.END) {
            schedulerTick.invokeExact(Scheduler.server());
            ASMHooks.tickIEEPCompanionsServerSide();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientTick(TickEvent.ClientTickEvent event) throws Throwable {
        if (event.phase == TickEvent.Phase.END) {
            schedulerTick.invokeExact(Scheduler.client());
        }
    }

}
