package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asmproxy.Getter;
import de.take_weiland.mods.commons.asmproxy.Setter;
import de.take_weiland.mods.commons.asmproxy.TargetClass;

@TargetClass("net.minecraft.client.renderer.EntityRenderer")
public interface EntityRendererProxy {

	@Getter(mcpName = ASMConstants.F_FOV_MODIFIER_HAND_MCP, obfName = ASMConstants.F_FOV_MODIFIER_HAND_OBF)
	float getFovHand();
	
	@Getter(mcpName = ASMConstants.F_FOV_MODIFIER_HAND_PREV_MCP, obfName = ASMConstants.F_FOV_MODIFIER_HAND_PREV_OBF)
	float getFovHandPrev();
	
	@Setter(mcpName = ASMConstants.F_FOV_MODIFIER_HAND_MCP, obfName = ASMConstants.F_FOV_MODIFIER_HAND_OBF)
	void setFovHand(float fov);
	
	@Setter(mcpName = ASMConstants.F_FOV_MODIFIER_HAND_PREV_MCP, obfName = ASMConstants.F_FOV_MODIFIER_HAND_PREV_OBF)
	void setFovHandPrev(float fovPrev);
}
