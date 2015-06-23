package de.take_weiland.mods.commons.internal.client;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.take_weiland.mods.commons.client.ScreenWithParent;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.BiFunction;

import static net.minecraft.client.Minecraft.getMinecraft;

public final class ClientProxy implements SevenCommonsProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onOpenGui(GuiOpenEvent event) {
        if (event.gui == null && getMinecraft().currentScreen instanceof ScreenWithParent) {
            event.gui = ((ScreenWithParent) getMinecraft().currentScreen).getParentScreen();
        }
    }

    @Override
    public void sendPacketToServer(Packet p) {
        getMinecraft().getNetHandler().addToSendQueue(p);
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return getMinecraft().thePlayer;
    }

    @Override
    public NetworkManager getClientNetworkManager() {
        return getMinecraft().getNetHandler().getNetworkManager();
    }

    @Override
    public BiFunction<String, byte[], ? extends Packet> getC17PacketCstr() {
        return C17PacketCustomPayload::new;
    }
}
