package de.take_weiland.mods.commons.util;

import java.util.List;

import net.minecraft.nbt.NBTBase;
import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asmproxy.Getter;
import de.take_weiland.mods.commons.asmproxy.TargetClass;

@TargetClass("net.minecraft.nbt.NBTTagList")
public interface NBTListProxy {

	@Getter(mcpName = ASMConstants.F_TAG_LIST_MCP, obfName = ASMConstants.F_TAG_LIST_OBF)
	public <T extends NBTBase> List<T> getWrappedList();
	
}
