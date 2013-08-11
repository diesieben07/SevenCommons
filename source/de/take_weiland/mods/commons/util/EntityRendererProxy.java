package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.asmproxy.Getter;
import de.take_weiland.mods.commons.asmproxy.Setter;
import de.take_weiland.mods.commons.asmproxy.TargetClass;

@TargetClass("net.minecraft.client.renderer.EntityRenderer")
public interface EntityRendererProxy {

	@Getter(mcpName = "fovModifierHand", obfName = "S")
	float getFovHand();
	
	@Getter(mcpName = "fovModifierHandPrev", obfName = "T")
	float getFovHandPrev();
	
	@Setter(mcpName = "fovModifierHand", obfName = "S")
	void setFovHand(float fov);
	
	@Setter(mcpName = "fovModifierHandPrev", obfName = "T")
	void setFovHandPrev(float fovPrev);
}
