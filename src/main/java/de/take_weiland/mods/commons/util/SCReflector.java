package de.take_weiland.mods.commons.util;

import static de.take_weiland.mods.commons.asm.ASMConstants.*;

import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.entity.EntityTracker;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import de.take_weiland.mods.commons.fastreflect.Getter;
import de.take_weiland.mods.commons.fastreflect.Setter;

public interface SCReflector {

	@Getter(names = { F_TAG_LIST_MCP, F_TAG_LIST_OBF })
	<T extends NBTBase> List<T> getWrappedList(NBTTagList list);
	
	@Getter(names = { F_FOV_MODIFIER_HAND_MCP, F_FOV_MODIFIER_HAND_OBF })
	float getFovHand(EntityRenderer e);
	
	@Getter(names = { F_FOV_MODIFIER_HAND_PREV_MCP, F_FOV_MODIFIER_HAND_PREV_OBF })
	float getFovHandPrev(EntityRenderer e);
	
	@Setter(names = { F_FOV_MODIFIER_HAND_MCP, F_FOV_MODIFIER_HAND_OBF })
	void setFovHand(EntityRenderer e, float fov);
	
	@Setter(names = { F_FOV_MODIFIER_HAND_PREV_MCP, F_FOV_MODIFIER_HAND_PREV_OBF })
	void setFovHandPrev(EntityRenderer e, float fovPrev);
	
	@Getter(names = { F_TIMER_MCP, F_TIMER_SRG })
	Timer getTimer(Minecraft mc);
	
	@Getter(names = { F_TRACKED_ENTITY_IDS_MCP, F_TRACKED_ENTITY_IDS_OBF })
	IntHashMap getTrackerMap(EntityTracker tracker);
	
	@Getter(names = { F_MAP_TEXTURE_OBJECTS_MCP, F_MAP_TEXTURE_OBJECTS_OBF })
	Map<ResourceLocation, TextureObject> getTexturesMap(TextureManager manager);
	
}
