package de.take_weiland.mods.commons.util;

import net.minecraft.util.IntHashMap;
import de.take_weiland.mods.commons.asmproxy.Getter;
import de.take_weiland.mods.commons.asmproxy.TargetClass;

@TargetClass("net.minecraft.entity.EntityTracker")
public interface EntityTrackerProxy {
	
	@Getter(mcpName = "trackedEntityIDs", obfName = "c")
	IntHashMap getTrackerMap();

}
