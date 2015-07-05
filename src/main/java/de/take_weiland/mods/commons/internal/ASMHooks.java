package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.sync.IEEPSyncCompanion;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync.SyncCompanions;
import de.take_weiland.mods.commons.internal.sync.SyncEvent;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtFactories;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtHandler;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.*;

/**
 * A class containing methods called from ASM generated code.
 *
 * @author diesieben07
 */
@SuppressWarnings("unused")
public final class ASMHooks {

    public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/ASMHooks";
    public static final String ON_START_TRACKING = "onStartTracking";
    public static final String ON_PLAYER_CLONE = "onPlayerClone";
    public static final String NEW_SYNC_STREAM = "newSyncStream";
    public static final String SEND_SYNC_STREAM = "sendSyncStream";
    public static final String FIND_CONTAINER_INVS = "findContainerInvs";
    public static final String ON_LISTENER_ADDED = "onListenerAdded";
    public static final String IS_USEABLE_CLIENT = "isUseableClient";
    public static final String INVOKE_SYNC_COMP_CHECK = "invokeSyncCompanionCheck";
    public static final String ON_GUI_INIT = "onGuiInit";

    private ASMHooks() {
    }

    public static void invokeSyncCompanionCheck(Object obj, SyncCompanion companion) {
        if (companion != null) {
            SyncEvent event = companion.check(obj, false);
            if (event != null) {
                event.send(obj);
            }
        }
    }

    public static final String TICK_IEEP_COMPANIONS = "tickIEEPCompanions";

    public static void tickIEEPCompanions(List<IEEPSyncCompanion> props) {
        if (props != null) {
            // put actual logic into different method, to make this method smaller and more likely
            // to be inlined into the World class (called from there every tick for every entity!)
            tickIEEPCompanionsNonNull(props);
        }
    }

    private static void tickIEEPCompanionsNonNull(List<IEEPSyncCompanion> props) {
        int i = props.size();
        do {
            if (--i < 0) {
                return;
            }

            IEEPSyncCompanion companion = props.get(i);
            companion.check(companion._sc$ieep, false);
        } while (true);
    }

    public static final String ON_NEW_ENTITY_PROPS = "onNewEntityProps";

    public static void onNewEntityProps(Entity entity, IExtendedEntityProperties props, String identifier) throws Throwable {
        IEEPSyncCompanion companion = (IEEPSyncCompanion) SyncCompanions.newCompanion(props.getClass());
        if (companion == null) {
            return;
        }

        companion._sc$ieep = props;
        companion._sc$entity = entity;
        companion._sc$ident = identifier;
        List<IEEPSyncCompanion> companions = ((EntityProxy) entity)._sc$getPropsCompanions();
        if (companions == null) {
            companions = new ArrayList<>();
            ((EntityProxy) entity)._sc$setPropsCompanions(companions);
        }
        companions.add(companion);

        ieepCompanions.put(props, companion);
    }

    public static final String GET_IEEP_COMPANION = "getIEEPCompanion";

    private static final Map<IExtendedEntityProperties, IEEPSyncCompanion> ieepCompanions;

    static {
        if (FMLLaunchHandler.side().isClient()) {
            ieepCompanions = new MapMaker().concurrencyLevel(2).makeMap();
        } else {
            ieepCompanions = new HashMap<>();
        }
    }

    public static IEEPSyncCompanion getIEEPCompanion(IExtendedEntityProperties props) {
        return ieepCompanions.get(props);
    }

    public static void onEntityUnload(Entity entity) {
        List<IEEPSyncCompanion> companions = ((EntityProxy) entity)._sc$getPropsCompanions();
        if (companions != null) {
            for (IEEPSyncCompanion companion : companions) {
                ieepCompanions.remove(companion._sc$ieep);
            }
        }
    }

    public static final String WRITE_NBT_HOOK = "writeToNbtHook";

    public static void writeToNbtHook(Object obj, NBTTagCompound nbt) {
        ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
        if (handler != null) {
            handler.write(obj, nbt);
        }
    }

    public static final String READ_NBT_HOOK = "readFromNbtHook";

    public static void readFromNbtHook(Object obj, NBTTagCompound nbt) {
        ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
        if (handler != null) {
            handler.read(obj, nbt);
        }
    }

    public static final String IEEP_READ_NBT_HOOK = "readFromNBTIEEP";

    public static void readFromNBTIEEP(Object obj, String ident, NBTTagCompound nbt) {
        ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
        if (handler != null) {
            handler.read(obj, nbt.getCompoundTag(ident));
        }
    }

    public static final String IEEP_WRITE_NBT_HOOK = "writeToNBTIEEP";

    public static void writeToNBTIEEP(Object obj, String ident, NBTTagCompound nbt) {
        ToNbtHandler handler = ToNbtFactories.handlerFor(obj.getClass());
        if (handler != null) {
            handler.write(obj, NBT.getOrCreateCompound(nbt, ident));
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean isUseableClient(Slot slot) {
        return slot != null && slot.canTakeStack(Minecraft.getMinecraft().thePlayer);
    }

    public static void onListenerAdded(Container container, ICrafting listener) {
        if (listener instanceof EntityPlayerMP) {
            List<IInventory> invs = Containers.getInventories(container).asList();
            for (int i = 0, len = invs.size(); i < len; i++) {
                IInventory inv = invs.get(i);
                if (inv instanceof NameableInventory && ((NameableInventory) inv).hasCustomName()) {
                    new PacketInventoryName(container.windowId, i, ((NameableInventory) inv).getCustomName()).sendTo((EntityPlayerMP) listener);
                }
            }
        }
    }

    public static ImmutableSet<IInventory> findContainerInvs(Container container) {
        IInventory last = null;
        ImmutableSet.Builder<IInventory> builder = ImmutableSet.builder();

        @SuppressWarnings("unchecked")
        List<Slot> slots = container.inventorySlots;
        for (Slot slot : slots) {
            if (slot.inventory != last) {
                builder.add((last = slot.inventory));
            }
        }
        return builder.build();
    }

}
