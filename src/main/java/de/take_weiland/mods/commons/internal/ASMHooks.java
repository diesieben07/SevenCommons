package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.SaveWorldsEvent;
import de.take_weiland.mods.commons.internal.sync.*;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtFactories;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtHandler;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing methods called from ASM generated code.
 *
 * @author diesieben07
 */
@SuppressWarnings({"unused", "ForLoopReplaceableByForEach"})
public final class ASMHooks {

    public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/ASMHooks";
    public static final String ON_START_TRACKING = "onStartTracking";
    public static final String ON_PLAYER_CLONE = "onPlayerClone";
    public static final String NEW_SYNC_STREAM = "newSyncStream";
    public static final String SEND_SYNC_STREAM = "sendSyncStream";
    public static final String FIND_CONTAINER_INVS = "findContainerInvs";
    public static final String ON_LISTENER_ADDED = "onListenerAdded";
    public static final String CAN_NUMBER_KEY_MOVE = "canNumberKeyMove";
    public static final String INVOKE_SYNC_COMP_CHECK = "invokeSyncCompanionCheck";

    public static final String ON_GUI_KEY = "onGuiKey";
    public static final String ON_GUI_MOUSE = "onGuiMouse";

    private ASMHooks() {
    }

    private static final Map<IExtendedEntityProperties, IEEPSyncCompanion> ieepCompanions;

    static {
        if (FMLLaunchHandler.side().isClient()) {
            ieepCompanions = new MapMaker().concurrencyLevel(2).makeMap();
        } else {
            ieepCompanions = new HashMap<>();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void onGuiKey(GuiScreen screen) {
        if (Keyboard.getEventKeyState()) {
            List<GuiTextField> list = ((GuiScreenProxy) screen)._sc$textFields();
            for (int i = 0, len = list.size(); i < len; i++) {
                list.get(i).textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void onGuiMouse(GuiScreen screen) {
        if (Mouse.getEventButtonState()) {
            int mouseX = Mouse.getEventX() * screen.width / screen.mc.displayWidth;
            int mouseY = screen.height - Mouse.getEventY() * screen.height / screen.mc.displayHeight - 1;
            int button = Mouse.getEventButton();

            List<GuiTextField> list = ((GuiScreenProxy) screen)._sc$textFields();
            for (int i = 0, len = list.size(); i < len; i++) {
                list.get(i).mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public static final String FIRE_WORLD_SAVE = "fireWorldSaveEvent";

    public static void fireWorldSaveEvent(boolean dontLog) {
        MinecraftForge.EVENT_BUS.post(new SaveWorldsEvent(!dontLog));
    }

    @SideOnly(Side.CLIENT)
    public static void tickIEEPCompanionsClientSide() {
        ieepCompanions.forEach((props, companion) -> {
            if (!companion._sc$entity.worldObj.isRemote) {
                companion.check(props, false);
            }
        });
    }

    @SideOnly(Side.SERVER)
    public static void tickIEEPCompanionsServerSide() {
        ieepCompanions.forEach((props, companion) -> companion.check(props, false));
    }

    public static void invokeSyncCompanionCheck(Object obj, SyncCompanion companion) {
        if (companion != null) {
            SyncEvent event = companion.check(obj, false);
        }
    }

    public static final String ON_NEW_ENTITY_PROPS = "onNewEntityProps";

    public static void onNewEntityProps(Entity entity, IExtendedEntityProperties props, String identifier) throws Throwable {
        IEEPSyncCompanion companion = (IEEPSyncCompanion) SyncCompanions.newCompanion(props.getClass());
        if (companion == null) {
            return;
        }

        companion._sc$entity = entity;
        companion._sc$ident = identifier;
        ieepCompanions.put(props, companion);
    }

    public static final String GET_IEEP_COMPANION = "getIEEPCompanion";

    public static IEEPSyncCompanion getIEEPCompanion(IExtendedEntityProperties props) {
        return ieepCompanions.get(props);
    }

    public static void onEntityUnload(Entity entity) {
        ieepCompanions.keySet().removeAll(((EntityProxy) entity)._sc$getIEEPMap().values());
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
    public static boolean canNumberKeyMove(Slot slot) {
        if (slot == null) {
            return true;
        }
        if (!slot.canTakeStack(Minecraft.getMinecraft().thePlayer)) {
            return false;
        }

        GuiScreenProxy screen = (GuiScreenProxy) Minecraft.getMinecraft().currentScreen;
        if (screen == null) {
            return false; // this can be true if GuiScreen.keyTyped closes the screen -.-
        }
        List<GuiTextField> fields = screen._sc$textFields();
        for (int i = 0, len = fields.size(); i < len; i++) {
            GuiTextField field = fields.get(i);
            if (field.isFocused()) {
                return false;
            }
        }
        return true;
    }

    public static void onListenerAdded(Container container, ICrafting listener) throws Throwable {
        if (listener instanceof EntityPlayerMP) {
            List<IInventory> invs = Containers.getInventories(container).asList();
            for (int i = 0, len = invs.size(); i < len; i++) {
                IInventory inv = invs.get(i);
                if (inv instanceof NameableInventory && ((NameableInventory) inv).hasCustomName()) {
                    new PacketInventoryName(container.windowId, i, ((NameableInventory) inv).getCustomName()).sendTo((EntityPlayerMP) listener);
                }
                if (inv instanceof PlayerAwareInventory) {
                    ((PlayerAwareInventory) inv)._sc$onPlayerViewContainer(container, i, (EntityPlayerMP) listener);
                }
            }
            SyncCompanion companion = ((SyncedObjectProxy) container)._sc$getCompanion();
            if (companion != null) {
                companion.forceUpdate(container, false, (EntityPlayerMP) listener);
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

    public static final String ON_SLOT_ADDED = "onSlotAdded";

    public static void onSlotAdded(Container container, Slot slot) {
        if (slot instanceof ContainerAwareSlot) {
            ((ContainerAwareSlot) slot)._sc$injectContainer(container);
        }
    }

}
