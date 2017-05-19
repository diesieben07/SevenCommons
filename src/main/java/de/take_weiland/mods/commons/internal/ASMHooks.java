package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.SaveWorldsEvent;
import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync_olds.SyncedObjectProxy;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtFactories;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtHandler;
import de.take_weiland.mods.commons.inv.Containers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

/**
 * A class containing methods called from ASM generated code.
 *
 * @author diesieben07
 */
@SuppressWarnings({"unused", "ForLoopReplaceableByForEach"})
public final class ASMHooks {

    public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/ASMHooks";
    public static final String ON_START_TRACKING = "onStartTracking";
    public static final String ON_PLAYER_CLONE           = "onPlayerClone";
    public static final String NEW_SYNC_STREAM           = "newSyncStream";
    public static final String SEND_SYNC_STREAM          = "sendSyncStream";
    public static final String FIND_CONTAINER_INVS       = "findContainerInvs";
    public static final String ON_LISTENER_ADDED         = "onListenerAdded";
    public static final String CAN_NUMBER_KEY_MOVE       = "canNumberKeyMove";
    public static final String INVOKE_SYNC_COMP_CHECK    = "invokeSyncCompanionCheck";
    public static final String TICK_CONTAINER_COMPANIONS = "tickContainerCompanions";

    public static final String ON_GUI_KEY     = "onGuiKey";
    public static final String ON_GUI_MOUSE   = "onGuiMouse";
    public static final String DRAW_BLOCK_INV = "drawBlockInv";

    public static boolean drawBlockInv;

    private ASMHooks() {
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

    public static void invokeSyncCompanionCheck(Object obj, SyncCompanion companion) {
        if (companion != null) {
            companion.check(obj, 0, null);
        }
    }

    public static void tickContainerCompanions(Container container) {
        EntityPlayer player = Containers.getViewer(container);
        if (player == null || player.world.isRemote) {
            return;
        }

        SyncCompanion companion = ((SyncedObjectProxy) container)._sc$getCompanion();
        if (companion != null) {
            companion.check(container, 0, null);
        }

        ImmutableList<IInventory> list = ((ContainerProxy) container)._sc$getInventories().asList();

        for (int i = 0, len = list.size(); i < len; i++) {
            IInventory inv = list.get(i);
            if (inv instanceof SyncedObjectProxy) {
                companion = ((SyncedObjectProxy) inv)._sc$getCompanion();
                if (companion != null) {
                    companion.checkInContainer(inv, 0, (EntityPlayerMP) player);
                }
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

    @SideOnly(Side.CLIENT)
    public static boolean canNumberKeyMove(Slot slot) {
        if (slot == null) {
            return true;
        }
        if (!slot.canTakeStack(Minecraft.getMinecraft().player)) {
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

    public static void onListenerAdded(Container container, IContainerListener listener) throws Throwable {
        if (listener instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) listener;

            // TODO!
            List<IInventory> invs = Containers.getInventories(container).asList();
            for (int i = 0, len = invs.size(); i < len; i++) {
                IInventory inv = invs.get(i);
                if (inv.hasCustomName()) {
//                    SimplePacketKt.sendTo(new PacketInventoryName(container.windowId, i, inv.getDisplayName()), player);
                }
                if (inv instanceof PlayerAwareInventory) {
                    ((PlayerAwareInventory) inv)._sc$onPlayerViewContainer(container, i, player);
                }
                if (inv instanceof SyncedObjectProxy) {
                    SyncCompanion companion = ((SyncedObjectProxy) inv)._sc$getCompanion();
                    if (companion != null) {
                        companion.checkInContainer(inv, SyncCompanion.FORCE_CHECK, player);
                    }
                }
            }

            SyncCompanion companion = ((SyncedObjectProxy) container)._sc$getCompanion();
            if (companion != null) {
                companion.check(container, SyncCompanion.FORCE_CHECK, (EntityPlayerMP) listener);
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
