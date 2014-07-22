package de.take_weiland.mods.commons.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.reflect.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.entity.EntityTracker;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static de.take_weiland.mods.commons.asm.MCPNames.*;

/**
 * Accessor interface for various private fields and methods around the Minecraft code.<br />
 * If the field/method to access is static, the instance parameter is ignored, null can be passed.<br />
 *
 * @see de.take_weiland.mods.commons.reflect.SCReflection
 */
public interface SCReflector {

	public static final SCReflector instance = SCReflection.createAccessor(SCReflector.class);

	/**
	 * For cleaner code use {@link de.take_weiland.mods.commons.nbt.NBT#asList(net.minecraft.nbt.NBTTagList)}
	 */
	@Unsafe
	@Getter(field = F_TAG_LIST, srg = true)
	<T extends NBTBase> List<T> getWrappedList(NBTTagList list);

	@Unsafe
	@Getter(field = F_TAG_MAP, srg = true)
	Map<String, NBTBase> getWrappedMap(NBTTagCompound nbt);

	@SideOnly(Side.CLIENT)
	@Getter(field = F_FOV_MODIFIER_HAND, srg = true)
	float getFovHand(EntityRenderer e);

	@SideOnly(Side.CLIENT)
	@Setter(field = F_FOV_MODIFIER_HAND, srg = true)
	void setFovHand(EntityRenderer e, float fov);

	@SideOnly(Side.CLIENT)
	@Getter(field = F_FOV_MODIFIER_HAND_PREV, srg = true)
	float getFovHandPrev(EntityRenderer e);

	@SideOnly(Side.CLIENT)
	@Setter(field = F_FOV_MODIFIER_HAND_PREV, srg = true)
	void setFovHandPrev(EntityRenderer e, float fovPrev);

	@SideOnly(Side.CLIENT)
	@Getter(field = F_TIMER, srg = true)
	Timer getTimer(Minecraft mc);

	@Getter(field = F_TRACKED_ENTITY_IDS, srg = true)
	IntHashMap getTrackerMap(EntityTracker tracker);

	@SideOnly(Side.CLIENT)
	@Getter(field = F_MAP_TEXTURE_OBJECTS, srg = true)
	Map<ResourceLocation, TextureObject> getTexturesMap(TextureManager manager);

	@Unsafe
	@Getter(field = F_PACKET_CLASS_TO_ID_MAP, srg = true)
	Map<Class<? extends Packet>, Integer> getClassToIdMap(Packet dummy);

	@SideOnly(Side.CLIENT)
	@Getter(field = F_BUTTON_LIST, srg = true)
	List<GuiButton> getButtonList(GuiScreen screen);

	@Getter(field = F_ITEM_DAMAGE, srg = true)
	int getRawDamage(ItemStack stack);

	@Getter(field = F_UNLOCALIZED_NAME_BLOCK, srg = true)
	String getRawUnlocalizedName(Block block);

	@Invoke(method = M_SET_HAS_SUBTYPES, srg = true)
	Item setHasSubtypes(Item instance, boolean value);

	@Getter(field = F_ICON_STRING, srg = true)
	String getRawIconName(Item item);

	@Getter(field = F_UNLOCALIZED_NAME_ITEM, srg = true)
	String getRawUnlocalizedName(Item item);

	@Getter(field = F_TEXTURE_NAME_BLOCK, srg = true)
	String getRawIconName(Block block);

	@Invoke(method = M_GET_ICON_STRING, srg = true)
	String getIconName(Item item);

	@Invoke(method = M_GET_TEXTURE_NAME, srg = true)
	String getIconName(Block block);

	@SideOnly(Side.CLIENT)
	@Invoke(method = M_ACTION_PERFORMED, srg = true)
	void actionPerformed(GuiScreen screen, GuiButton button);

	@SideOnly(Side.CLIENT)
	@Getter(field = F_Z_LEVEL, srg = true)
	float getZLevel(Gui gui);

	@Invoke(method = M_ADD_SLOT_TO_CONTAINER, srg = true)
	Slot addSlot(Container container, Slot slot);

	@Invoke(method = M_MERGE_ITEM_STACK, srg = true)
	boolean mergeItemStack(Container container, ItemStack stack, int slotStart, int slotEnd, boolean direction);

	@Getter(field = F_CRAFTERS, srg = true)
	List<ICrafting> getCrafters(Container container);

	@Invoke(method = "writeUTF")
	int writeUTF(DataOutputStream dummy, String s, DataOutput out) throws IOException;

	@Invoke(method = M_NBT_WRITE, srg = true)
	void write(NBTBase nbt, DataOutput out) throws IOException;

	@Invoke(method = M_NBT_LOAD, srg = true)
	void load(NBTBase nbt, DataInput in, int depth) throws IOException;

	@Unsafe
	@Construct
	String createStringShared(char[] arr, boolean dummy);

}
