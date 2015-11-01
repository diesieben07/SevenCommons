package de.take_weiland.mods.commons.internal.transformers;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.net.BaseModPacket;
import de.take_weiland.mods.commons.internal.net.PacketAdditionalMethods;
import de.take_weiland.mods.commons.internal.net.RawPacketAdditionalMethods;
import de.take_weiland.mods.commons.internal.transformers.net.InterfaceAdder;
import de.take_weiland.mods.commons.internal.transformers.net.PacketGetDataOptimizer;
import de.take_weiland.mods.commons.internal.transformers.net.SimplePacketWithResponseTransformer;
import de.take_weiland.mods.commons.internal.transformers.sync.*;
import de.take_weiland.mods.commons.internal.transformers.tonbt.EntityNBTHook;
import de.take_weiland.mods.commons.internal.transformers.tonbt.TileEntityNBTHook;
import org.objectweb.asm.ClassVisitor;

import java.util.function.Predicate;

/**
 * @author diesieben07
 */
public final class SCVisitorTransformerWrapper extends VisitorBasedTransformer {

    private static final boolean DEBUG = false;

    @Override
    protected void addEntries() {
        // general MC hooks
        addEntry(ContainerTransformer::new, "net/minecraft/inventory/Container");
        addEntry(InventoryNumberKeysFix::new, "net/minecraft/client/gui/inventory/GuiContainer", MCPNames.method(SRGConstants.M_CHECK_HOTBAR_KEYS));
        addEntry(SaveWorldsEventHook::new, "net/minecraft/server/MinecraftServer", MCPNames.method(SRGConstants.M_SAVE_ALL_WORLDS));
        addEntry(ContainerSlotDrawHook::new, "net/minecraft/client/gui/inventory/GuiContainer");

        addEntry(GuiScreenHooks::new, "net/minecraft/client/gui/GuiScreen");

        // @Sync hooks
        addEntry(CompanionFieldAdder::new,
                "net/minecraft/tileentity/TileEntity",
                "net/minecraft/entity/Entity",
                "net/minecraft/inventory/Container");
        addEntry(TileEntityTickHook::new, "net/minecraft/world/World");
        addEntry(EntityTickHook::new, "net/minecraft/world/World");
        addEntry(ContainerSyncHooks::new, "net/minecraft/inventory/Container");
        addEntry(EntitySyncPropsHooks::new, "net/minecraft/entity/Entity");
        addEntry(IEEPSyncTransformer::new, "net/minecraftforge/common/IExtendedEntityProperties");

        // @ToNbt hooks
        addEntry(EntityNBTHook::new, "net/minecraft/entity/Entity");
        addEntry(TileEntityNBTHook::new, "net/minecraft/tileentity/TileEntity");

        // packet stuff
        addEntry(SimplePacketWithResponseTransformer::new, "de/take_weiland/mods/commons/net/SimplePacket$WithResponse");

        //noinspection PointlessBooleanExpression,ConstantConditions
        if (!MCPNames.use() || DEBUG) {
            // don't transform people's classes inside dev env, hurts hotswapping :D
            addEntry(PacketGetDataOptimizer::new, apacheFilter());
        }

        addEntry((ClassVisitor cv) -> new InterfaceAdder(cv, BaseModPacket.CLASS_NAME),
                "de/take_weiland/mods/commons/net/Packet",
                "de/take_weiland/mods/commons/net/Packet$WithResponse");

        addEntry(cv -> new InterfaceAdder(cv, PacketAdditionalMethods.CLASS_NAME), "de/take_weiland/mods/commons/net/Packet");
        addEntry(cv -> new InterfaceAdder(cv, RawPacketAdditionalMethods.CLASS_NAME), "de/take_weiland/mods/commons/net/RawPacket");

        // misc
        addEntry(ListenableSupport::new, apacheFilter());
        if (FMLLaunchHandler.side().isServer()) {
            addEntry(SideOfOptimizer::new, "de/take_weiland/mods/commons/util/Sides", "sideOf");
        }


    }

    private static Predicate<String> apacheFilter() {
        return clazz -> !clazz.startsWith("org/apache/");
    }
}
