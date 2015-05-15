package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.SerializationMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Apply this to a field in your {@link net.minecraft.tileentity.TileEntity}, {@link net.minecraft.entity.Entity} or {@link net.minecraftforge.common.IExtendedEntityProperties}
 * to automatically save and load it from NBT.</p>
 * <p>Supports all primitives, Enums, Strings and implementors of {@link de.take_weiland.mods.commons.nbt.NBTSerializable} as well as arrays thereof, even if not directly supported by NBT.</p>
 * <ul>
 * <li>Primitives, Strings, int[], byte[] are saved as-is with the corresponding methods in {@link net.minecraft.nbt.NBTTagCompound}</li>
 * <li>boolean[] are saved as a byte[] of bit flags (every byte contains up to 8 booleans). For any key, e.g. "someKey" it also generates the integer "someKey_sc$boolArrLen"
 * to accurately reconstruct the array.</li>
 * <li>long[] are saved as an int[] of twice the size, two integers making up one long.</li>
 * <li>float[] and double[] are saved as an int[] resp. long[] after applying {@link java.lang.Float#floatToIntBits(float)} resp. {@link java.lang.Double#doubleToLongBits(double)}</li>
 * <li>String[] are saved as a {@link net.minecraft.nbt.NBTTagList} containing the Strings</li>
 * <li>Enums and Enum[] are saved as a String resp. String[] containing the names of the Enum constants ({@link Enum#name()})</li>
 * </ul>
 *
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ToNbt {

    /**
     * the key to use for this field. Empty String (default) uses the field/method name
     */
    String key() default "";

    SerializationMethod.Method method() default SerializationMethod.Method.DEFAULT;

}
