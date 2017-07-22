package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.SaveWorldsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
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

    public static final String WRITE_NBT_HOOK = "writeToNbtHook";



    public static final String READ_NBT_HOOK = "readFromNbtHook";



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
