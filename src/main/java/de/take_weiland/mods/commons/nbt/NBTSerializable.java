package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author diesieben07
 */
public interface NBTSerializable {

	NBTBase serialize();

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Deserializer {

	}

}
