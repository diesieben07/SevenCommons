package de.take_weiland.mods.commons.util;

import net.minecraft.util.IntHashMap;
import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asmproxy.Getter;
import de.take_weiland.mods.commons.asmproxy.TargetClass;

@TargetClass("net.minecraft.entity.EntityTracker")
public interface EntityTrackerProxy {
	
	@Getter(mcpName = ASMConstants.F_TRACKED_ENTITY_IDS_MCP, obfName = ASMConstants.F_TRACKED_ENTITY_IDS_OBF)
	IntHashMap getTrackerMap();

}
