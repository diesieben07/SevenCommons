package de.take_weiland.mods.commons.internal.transformers;

import java.util.function.Predicate;

/**
 * @author diesieben07
 */
public final class SCVisitorTransformerWrapper extends VisitorBasedTransformer {

    private static final boolean DEBUG = false;

    @Override
    protected void addEntries() {
        // general MC hooks
//        addEntry(ContainerTransformer::new, "net/minecraft/inventory/Container");
//        addEntry(InventoryNumberKeysFix::new, "net/minecraft/client/gui/inventory/GuiContainer", MCPNames.method(SRGConstants.M_CHECK_HOTBAR_KEYS));
//        addEntry(SaveWorldsEventHook::new, "net/minecraft/server/MinecraftServer", MCPNames.method(SRGConstants.M_SAVE_ALL_WORLDS));
//        addEntry(ContainerSlotDrawHook::new, "net/minecraft/client/gui/inventory/GuiContainer");
//        addEntry(cv -> new InterfaceAdder(cv, "de/take_weiland/mods/commons/client/icon/IconProviderAdd"), "de/take_weiland/mods/commons/client/icon/IconProvider");
//        addEntry(PlayerManagerHook::new, PlayerManagerHook.PLAYER_MANAGER_CLASS);
//        addEntry(PlayerManagerHook.PlayerInstanceHook::new, PlayerManagerHook.PLAYER_INSTANCE_CLASS);
//        addEntry(WorldHook::new, WorldHook.WORLD_CLASS_NAME);
//
//        addEntry(FieldAdder.cstr(ChunkProxy.class), "net/minecraft/world/chunk/Chunk");
//        addEntry(FieldAdder.cstr(WorldServerProxy.class), "net/minecraft/world/WorldServer");
//        addEntry(FieldAdder.cstr(EntityPlayerMPProxy.class), "net/minecraft/entity/player/EntityPlayerMP");
//
//        addEntry(FieldAdder.cstr(VanillaPacketProxy.class), PacketEncoderHook.PACKET);
//        addEntry(cv -> new InterfaceAdder(cv, "de/take_weiland/mods/commons/internal/VanillaSimplePacketShim"), PacketEncoderHook.PACKET);
//        addEntry(ServerConfigManagerHook::new, "net/minecraft/server/management/ServerConfigurationManager");
//
//        addEntry(PacketEncoderHook::new, PacketEncoderHook.MESSAGE_SERIALIZER);
//        addEntry(PacketDecoderHook::new, PacketDecoderHook.MESSAGE_DESERIALIZER);
//
//        if (FMLLaunchHandler.side().isClient()) {
//            addEntry(GuiScreenHooks::new, "net/minecraft/client/gui/GuiScreen");
//            addEntry(EntityRendererHook::new, EntityRendererHook.ENTITY_RENDERER_CLASS);
//            addEntry(MinecraftHook::new, MinecraftHook.MINECRAFT_CLASS);
//            addEntry(NetworkManagerHook::new, NetworkManagerHook.NETWORK_MANAGER_CLASS);
//            addEntry(cv -> new InterfaceAdder(cv, "de/take_weiland/mods/commons/util/GuiConstructorInternal"), "de/take_weiland/mods/commons/util/GuiIdentifier$GuiContainerConstructor");
//            addEntry(cv -> new InterfaceAdder(cv, "de/take_weiland/mods/commons/util/GuiConstructorInternal$OnSingleGui"), "de/take_weiland/mods/commons/util/GuiIdentifier$GuiConstructor");
//        }
//
//        // @Sync hooks
//        addEntry(CompanionFieldAdder::new,
//                "net/minecraft/tileentity/TileEntity",
//                "net/minecraft/entity/Entity",
//                "net/minecraft/inventory/Container");
//        addEntry(TileEntityTickHook::new, "net/minecraft/world/World");
//        addEntry(EntityTickHook::new, "net/minecraft/world/World");
//        addEntry(ContainerSyncHooks::new, "net/minecraft/inventory/Container");
//
//        // @ToNbt hooks
//        addEntry(TileEntityNBTHook::new, "net/minecraft/tileentity/TileEntity");
//
//        // packet stuff
//        addEntry(SimplePacketWithResponseTransformer::new, "de/take_weiland/mods/commons/net/SimplePacket$WithResponse");
//
//        // misc
//        addEntry(ListenableSupport::new, apacheFilter());
    }

    private static Predicate<String> apacheFilter() {
        return clazz -> !clazz.startsWith("org/apache/");
    }
}
