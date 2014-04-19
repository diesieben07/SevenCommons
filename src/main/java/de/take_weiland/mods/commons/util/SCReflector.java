package de.take_weiland.mods.commons.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.fastreflect.Getter;
import de.take_weiland.mods.commons.fastreflect.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.entity.EntityTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;

import java.util.List;
import java.util.Map;

import static de.take_weiland.mods.commons.internal.ASMConstants.*;

/**
 * Accessor interface for various private fields and methods around the Minecraft code.<br />
 * If the field/method to access is static, the instance parameter is ignored, null can be passed.<br />
 * Obtain an Implementation of this interface with {@link MiscUtil#getReflector()}
 * @see de.take_weiland.mods.commons.fastreflect.Fastreflect
 */
public interface SCReflector {

	/**
	 * For cleaner code use {@link de.take_weiland.mods.commons.util.NBT#asList(net.minecraft.nbt.NBTTagList)}
	 */
	@Unsafe
	@Getter(field = { F_TAG_LIST_MCP, F_TAG_LIST_SRG })
	<T extends NBTBase> List<T> getWrappedList(NBTTagList list);

	@Unsafe
	@Getter(field = { F_TAG_MAP_MCP, F_TAG_MAP_SRG })
	Map<String, NBTBase> getWrappedMap(NBTTagCompound nbt);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_FOV_MODIFIER_HAND_MCP, F_FOV_MODIFIER_HAND_SRG})
	float getFovHand(EntityRenderer e);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_FOV_MODIFIER_HAND_PREV_MCP, F_FOV_MODIFIER_HAND_PREV_SRG})
	float getFovHandPrev(EntityRenderer e);
	
	@SideOnly(Side.CLIENT)
	@Setter(field = { F_FOV_MODIFIER_HAND_MCP, F_FOV_MODIFIER_HAND_SRG})
	void setFovHand(EntityRenderer e, float fov);
	
	@SideOnly(Side.CLIENT)
	@Setter(field = { F_FOV_MODIFIER_HAND_PREV_MCP, F_FOV_MODIFIER_HAND_PREV_SRG})
	void setFovHandPrev(EntityRenderer e, float fovPrev);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_TIMER_MCP, F_TIMER_SRG })
	Timer getTimer(Minecraft mc);
	
	@Getter(field = { F_TRACKED_ENTITY_IDS_MCP, F_TRACKED_ENTITY_IDS_SRG})
	IntHashMap getTrackerMap(EntityTracker tracker);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_MAP_TEXTURE_OBJECTS_MCP, F_MAP_TEXTURE_OBJECTS_SRG})
	Map<ResourceLocation, TextureObject> getTexturesMap(TextureManager manager);
	
	@Unsafe
	@Getter(field = { F_PACKET_CLASS_TO_ID_MAP_MCP, F_PACKET_CLASS_TO_ID_MAP_SRG })
	Map<Class<? extends Packet>, Integer> getClassToIdMap(Packet dummy);

	@SideOnly(Side.CLIENT)
	@Getter(field = { F_IS_ENABLED_MCP, F_IS_ENABLED_SRG })
	boolean isEnabled(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = { F_DISABLED_COLOR_MCP, F_DISABLED_COLOR_SRG })
	int getDisabledColor(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = { F_ENABLED_COLOR_MCP, F_ENABLED_COLOR_SRG })
	int getEnabledColor(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = { F_CAN_LOOSE_FOCUS_MCP, F_CAN_LOOSE_FOCUS_SRG })
	boolean canLooseFocus(GuiTextField textField);

	@SideOnly(Side.CLIENT)
	@Getter(field = { F_BUTTON_LIST_MCP, F_BUTTON_LIST_SRG})
	List<GuiButton> getButtonList(GuiScreen screen);

	@Getter(field = { F_ITEM_DAMAGE_MCP, F_ITEM_DAMAGE_SRG })
	int getRawDamage(ItemStack stack);

	@Getter(field = {F_UNLOCALIZED_NAME_BLOCK_MCP, F_UNLOCALIZED_NAME_BLOCK_SRG})
	String getRawUnlocalizedName(Block block);

}
