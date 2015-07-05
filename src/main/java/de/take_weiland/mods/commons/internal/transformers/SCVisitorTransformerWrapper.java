package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.transformers.net.PacketTransformer;
import de.take_weiland.mods.commons.internal.transformers.net.SimplePacketWithResponseTransformer;
import de.take_weiland.mods.commons.internal.transformers.sync.*;
import de.take_weiland.mods.commons.internal.transformers.tonbt.EntityNBTHook;
import de.take_weiland.mods.commons.internal.transformers.tonbt.TileEntityNBTHook;

/**
 * @author diesieben07
 */
public final class SCVisitorTransformerWrapper extends VisitorBasedTransformer {
    @Override
    protected void addEntries() {
        addEntry(CompanionFieldAdder::new,
                "net/minecraft/tileentity/TileEntity",
                "net/minecraft/entity/Entity",
                "net/minecraft/inventory/Container");

        // @Sync hooks
        addEntry(TileEntityTickHook::new, "net/minecraft/world/World");
        addEntry(EntityTickHook::new, "net/minecraft/world/World");
        addEntry(ContainerTickHook::new, "net/minecraft/inventory/Container");
        addEntry(EntitySyncPropsHooks::new, "net/minecraft/entity/Entity");

        // @ToNbt hooks
        addEntry(EntityNBTHook::new, "net/minecraft/entity/Entity");
        addEntry(TileEntityNBTHook::new, "net/minecraft/tileentity/TileEntity");

        addEntry(ContainerTransformer::new, "net/minecraft/inventory/Container");

        addEntry(InventoryNumberKeysFix::new,
                "net/minecraft/client/gui/inventory/GuiContainer", MCPNames.method(SRGConstants.M_CHECK_HOTBAR_KEYS));

        addEntry(ListenableSupport::new, clazz -> !clazz.startsWith("org/apache/"));
        addEntry(IEEPSyncTransformer::new, "net/minecraftforge/common/IExtendedEntityProperties");

        // packet stuff
        addEntry(SimplePacketWithResponseTransformer::new, "de/take_weiland/mods/commons/net/SimplePacket$WithResponse");
        addEntry(PacketTransformer::new, "de/take_weiland/mods/commons/net/Packet");
    }
}
