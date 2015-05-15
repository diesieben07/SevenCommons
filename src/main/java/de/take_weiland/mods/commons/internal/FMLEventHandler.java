package de.take_weiland.mods.commons.internal;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.SCMessageHandlerClient;
import de.take_weiland.mods.commons.internal.net.SCMessageHandlerServer;
import de.take_weiland.mods.commons.util.Scheduler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.NetHandlerPlayServer;

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
        NetHandlerPlayServer handler = (NetHandlerPlayServer) event.handler;
        ChannelPipeline pipeline = handler.netManager.channel().pipeline();

        if (!event.isLocal) {
            insertEncoder(pipeline, NetworkImpl.TO_CLIENT_ENCODER);
        }

        insertHandler(pipeline, new SCMessageHandlerServer(handler.playerEntity));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        NetHandlerPlayClient handler = (NetHandlerPlayClient) event.handler;
        ChannelPipeline pipeline = handler.getNetworkManager().channel().pipeline();

        if (!event.isLocal) {
            // only need the encoder when not connected locally
            insertEncoder(pipeline, NetworkImpl.TO_SERVER_ENCODER);
        }
        // handler handles both direct messages (for local) and the vanilla-payload packet
        insertHandler(pipeline, SCMessageHandlerClient.INSTANCE);
    }

    private void insertHandler(ChannelPipeline pipeline, ChannelHandler handler) {
        pipeline.addBefore("packet_handler", "sevencommons:handler", handler);
    }

    private void insertEncoder(ChannelPipeline pipeline, ChannelHandler encoder) {
        // this is "backwards" - outbound messages travel "upwards" in the pipeline
        // so really the order is sevencommons:encoder and then vanilla's encoder
        pipeline.addAfter("encoder", "sevencommons:encoder", encoder);
    }

    static {
        Reflection.initialize(Scheduler.class);
        Runnable[] tickers = (Runnable[]) Launch.blackboard.get(SCHEDULER_TEMP_KEY);
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
