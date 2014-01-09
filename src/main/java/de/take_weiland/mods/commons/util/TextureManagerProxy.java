package de.take_weiland.mods.commons.util;

import java.util.Map;

import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.util.ResourceLocation;
import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asmproxy.Getter;
import de.take_weiland.mods.commons.asmproxy.TargetClass;

@TargetClass("net.minecraft.client.renderer.texture.TextureManager")
public interface TextureManagerProxy {

	@Getter(mcpName = ASMConstants.F_MAP_TEXTURE_OBJECTS_MCP, obfName = ASMConstants.F_MAP_TEXTURE_OBJECTS_OBF)
	Map<ResourceLocation, TextureObject> getTexturesMap();
	
}
