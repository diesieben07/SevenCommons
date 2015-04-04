package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.base.Predicate;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.transformers.sync.*;
import de.take_weiland.mods.commons.internal.transformers.tonbt.EntityNBTHook;
import de.take_weiland.mods.commons.internal.transformers.tonbt.TileEntityNBTHook;

/**
 * @author diesieben07
 */
public final class SCVisitorTransformerWrapper extends VisitorBasedTransformer {
    @Override
    protected void addEntries() {
        addEntry(CompanionFieldAdder.class,
                "net/minecraft/tileentity/TileEntity",
                "net/minecraft/entity/Entity",
                "net/minecraft/inventory/Container");

        // @Sync hooks
        addEntry(TileEntityTickHook.class, "net/minecraft/world/World");
        addEntry(EntityTickHook.class, "net/minecraft/world/World");
        addEntry(ContainerTickHook.class, "net/minecraft/inventory/Container");
        addEntry(EntitySyncPropsHooks.class, "net/minecraft/entity/Entity");

        // @ToNbt hooks
        addEntry(EntityNBTHook.class, "net/minecraft/entity/Entity");
        addEntry(TileEntityNBTHook.class, "net/minecraft/tileentity/TileEntity");

        addEntry(ContainerGetInventoriesSupport.class, "net/minecraft/inventory/Container");

        addEntry(PlayerCloneHook.class, "net/minecraft/entity/player/EntityPlayer", MCPNames.method(MCPNames.M_CLONE_PLAYER));

        addEntry(InventoryNumberKeysFix.class, "net/minecraft/client/gui/inventory/GuiContainer", MCPNames.method(MCPNames.M_CHECK_HOTBAR_KEYS));

        addEntry(InitGuiHook.class, "net/minecraft/client/gui/GuiScreen", MCPNames.method(MCPNames.M_SET_WORLD_AND_RESOLUTION));

        addEntry(EntityTrackingHook.class, "net/minecraft/entity/EntityTrackerEntry", MCPNames.method(MCPNames.M_TRY_START_WATCHING_THIS));

        if (ModPacketCstrAdder.isNeeded) {
            addEntry(ModPacketCstrAdder.class, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return !input.startsWith("net/minecraft");
                }
            });
        }
    }
}
