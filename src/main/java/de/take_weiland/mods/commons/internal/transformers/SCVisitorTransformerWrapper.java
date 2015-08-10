package de.take_weiland.mods.commons.internal.transformers;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.transformers.net.PacketGetDataOptimizer;
import de.take_weiland.mods.commons.internal.transformers.net.SimplePacketWithResponseTransformer;
import de.take_weiland.mods.commons.internal.transformers.sync.*;
import de.take_weiland.mods.commons.internal.transformers.tonbt.EntityNBTHook;
import de.take_weiland.mods.commons.internal.transformers.tonbt.TileEntityNBTHook;

import java.util.function.Predicate;

/**
 * @author diesieben07
 */
public final class SCVisitorTransformerWrapper extends VisitorBasedTransformer {

    private static final boolean DEBUG = true;

    @Override
    protected void addEntries() {
        // general MC hooks
        addEntry(ContainerTransformer::new, "net/minecraft/inventory/Container");
        addEntry(InventoryNumberKeysFix::new, "net/minecraft/client/gui/inventory/GuiContainer", MCPNames.method(SRGConstants.M_CHECK_HOTBAR_KEYS));
        addEntry(SaveWorldsEventHook::new, "net/minecraft/server/MinecraftServer", MCPNames.method(SRGConstants.M_SAVE_ALL_WORLDS));

        // @Sync hooks
        addEntry(CompanionFieldAdder::new,
                "net/minecraft/tileentity/TileEntity",
                "net/minecraft/entity/Entity",
                "net/minecraft/inventory/Container");
        addEntry(TileEntityTickHook::new, "net/minecraft/world/World");
        addEntry(EntityTickHook::new, "net/minecraft/world/World");
        addEntry(ContainerTickHook::new, "net/minecraft/inventory/Container");
        addEntry(EntitySyncPropsHooks::new, "net/minecraft/entity/Entity");
        addEntry(IEEPSyncTransformer::new, "net/minecraftforge/common/IExtendedEntityProperties");

        // @ToNbt hooks
        addEntry(EntityNBTHook::new, "net/minecraft/entity/Entity");
        addEntry(TileEntityNBTHook::new, "net/minecraft/tileentity/TileEntity");

        // packet stuff
        addEntry(SimplePacketWithResponseTransformer::new, "de/take_weiland/mods/commons/net/SimplePacket$WithResponse");

        // misc
        addEntry(ListenableSupport::new, apacheFilter());
        if (FMLLaunchHandler.side().isServer()) {
            addEntry(SideOfOptimizer::new, "de/take_weiland/mods/commons/util/Sides", "sideOf");
        }

        //noinspection PointlessBooleanExpression,ConstantConditions
        if (MCPNames.use() || DEBUG) {
            addEntry(PacketGetDataOptimizer::new, apacheFilter());
        }
    }

    private static Predicate<String> apacheFilter() {
        return clazz -> !clazz.startsWith("org/apache/");
    }
}
