package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import net.minecraft.entity.EntityTrackerEntry;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {


	{
		Reflection.initialize(EntityTrackerEntry.class);
	}


}
