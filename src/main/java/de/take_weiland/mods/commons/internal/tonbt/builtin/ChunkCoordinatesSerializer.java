package de.take_weiland.mods.commons.internal.tonbt.builtin;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
enum ChunkCoordinatesSerializer implements NBTSerializer.ForValue<ChunkCoordinates> {
    INSTANCE;

    @Override
    public ChunkCoordinates read(@Nonnull NBTBase nbt) {
        NBTTagCompound compound = (NBTTagCompound) nbt;
        return new ChunkCoordinates(
                compound.getInteger("x"),
                UnsignedBytes.toInt(compound.getByte("y")),
                compound.getInteger("z"));
    }

    @Override
    public NBTBase write(@Nonnull ChunkCoordinates value) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", value.posX);
        nbt.setByte("y", UnsignedBytes.checkedCast(value.posY));
        nbt.setInteger("z", value.posZ);
        return nbt;
    }
}
