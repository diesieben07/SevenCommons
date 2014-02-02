package de.take_weiland.mods.commons.util;

import static de.take_weiland.mods.commons.asm.ASMConstants.F_FOV_MODIFIER_HAND_MCP;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_FOV_MODIFIER_HAND_OBF;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_FOV_MODIFIER_HAND_PREV_MCP;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_FOV_MODIFIER_HAND_PREV_OBF;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_MAP_TEXTURE_OBJECTS_MCP;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_MAP_TEXTURE_OBJECTS_OBF;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_TAG_LIST_MCP;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_TAG_LIST_OBF;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_TIMER_MCP;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_TIMER_SRG;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_TRACKED_ENTITY_IDS_MCP;
import static de.take_weiland.mods.commons.asm.ASMConstants.F_TRACKED_ENTITY_IDS_OBF;

import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.entity.EntityTracker;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.fastreflect.Getter;
import de.take_weiland.mods.commons.fastreflect.Setter;

public interface SCReflector {

	@Getter(field = { F_TAG_LIST_MCP, F_TAG_LIST_OBF })
	<T extends NBTBase> List<T> getWrappedList(NBTTagList list);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_FOV_MODIFIER_HAND_MCP, F_FOV_MODIFIER_HAND_OBF })
	float getFovHand(EntityRenderer e);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_FOV_MODIFIER_HAND_PREV_MCP, F_FOV_MODIFIER_HAND_PREV_OBF })
	float getFovHandPrev(EntityRenderer e);
	
	@SideOnly(Side.CLIENT)
	@Setter(field = { F_FOV_MODIFIER_HAND_MCP, F_FOV_MODIFIER_HAND_OBF })
	void setFovHand(EntityRenderer e, float fov);
	
	@SideOnly(Side.CLIENT)
	@Setter(field = { F_FOV_MODIFIER_HAND_PREV_MCP, F_FOV_MODIFIER_HAND_PREV_OBF })
	void setFovHandPrev(EntityRenderer e, float fovPrev);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_TIMER_MCP, F_TIMER_SRG })
	Timer getTimer(Minecraft mc);
	
	@Getter(field = { F_TRACKED_ENTITY_IDS_MCP, F_TRACKED_ENTITY_IDS_OBF })
	IntHashMap getTrackerMap(EntityTracker tracker);
	
	@SideOnly(Side.CLIENT)
	@Getter(field = { F_MAP_TEXTURE_OBJECTS_MCP, F_MAP_TEXTURE_OBJECTS_OBF })
	Map<ResourceLocation, TextureObject> getTexturesMap(TextureManager manager);
	
	@Unsafe
	@Getter(field = "packetClassToIdMap")
	Map<Class<? extends Packet>, Integer> getClassToIdMap(Packet dummy);
	
}
